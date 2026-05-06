package es.uc3m.fintech.lesson10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix50sp2.MarketDataIncrementalRefresh;
import quickfix.fix50sp2.component.Instrument;

import java.util.*;

/**
 * Synthetic market data generator: drives a random walk for a fixed list
 * of symbols and pushes {@code MarketDataIncrementalRefresh} messages on
 * a dedicated publisher thread, one message per tick.
 */
public class MarketDataService {
    private static final String[] SYMBOLS = { "TSLA", "NVDA" };
    private static final char MD_ENTRY_TYPE_TRADE = '2';
    private static final char MD_UPDATE_ACTION_NEW = '0';
    private static final long UPDATE_INTERVAL_MS = 1000;

    private final Map<String, Double> currentPrices;
    private final Random random;
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataService.class);

    public MarketDataService() {
        this.currentPrices = new HashMap<>();
        this.currentPrices.put("TSLA", 400.0);
        this.currentPrices.put("NVDA", 200.0);
        // Fixed seed: deterministic price stream is useful when comparing FIX captures across runs.
        this.random = new Random(42);
    }

    public void init() {
        LOGGER.info("Market data service initialized - TSLA: {}, NVDA: {}",
                currentPrices.get("TSLA"), currentPrices.get("NVDA"));
    }

    private Map<String, Double> generateNextPrices() {
        Map<String, Double> newPrices = new HashMap<>();
        for (String symbol : SYMBOLS) {
            double variation = (random.nextDouble() - 0.5) * 4.0;
            double newPrice = Math.max(0.01, currentPrices.get(symbol) + variation);
            currentPrices.put(symbol, newPrice);
            newPrices.put(symbol, newPrice);
        }
        return newPrices;
    }

    public void start(SessionID sessionID, String mdReqId) {
        LOGGER.info("Starting continuous price updates for {}", mdReqId);

        new Thread(() -> {
            int updateCount = 0;
            while (!Thread.currentThread().isInterrupted()) {
                Map<String, Double> prices = generateNextPrices();
                sendPriceUpdate(sessionID, prices, mdReqId);
                updateCount++;

                if (updateCount % 10 == 0) {
                    LOGGER.info("Sent {} price updates", updateCount);
                }

                try {
                    Thread.sleep(UPDATE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    LOGGER.info("Price updates interrupted after {} updates", updateCount);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "PriceUpdateThread-" + mdReqId).start();
    }

    public MarketDataIncrementalRefresh createPriceUpdateMessage(Map<String, Double> prices, String mdReqId) {
        MarketDataIncrementalRefresh incrementalRefresh = new MarketDataIncrementalRefresh();
        incrementalRefresh.set(new MDReqID(mdReqId));

        for (String symbol : SYMBOLS) {
            MarketDataIncrementalRefresh.NoMDEntries entries = new MarketDataIncrementalRefresh.NoMDEntries();

            entries.set(new MDEntryPx(prices.get(symbol)));
            entries.set(new MDUpdateAction(MD_UPDATE_ACTION_NEW));
            entries.set(new MDEntryType(MD_ENTRY_TYPE_TRADE));

            Instrument instrument = new Instrument();
            instrument.set(new Symbol(symbol));
            entries.set(instrument);

            incrementalRefresh.addGroup(entries);
        }

        return incrementalRefresh;
    }

    private void sendPriceUpdate(SessionID sessionID, Map<String, Double> prices, String mdReqId) {
        MarketDataIncrementalRefresh incrementalRefresh = createPriceUpdateMessage(prices, mdReqId);

        LOGGER.info("Sent price updates - TSLA: {}, NVDA: {}", prices.get("TSLA"), prices.get("NVDA"));

        try {
            Session.sendToTarget(incrementalRefresh, sessionID);
        } catch (SessionNotFound e) {
            LOGGER.error("Session not found when sending price update", e);
        }
    }
}
