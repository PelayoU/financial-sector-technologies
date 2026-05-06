package es.uc3m.fintech.lesson10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix50sp2.MarketDataIncrementalRefresh;
import quickfix.fix50sp2.MarketDataRequest;
import quickfix.fix50sp2.MessageCracker;
import quickfix.fix50sp2.component.Instrument;

/**
 * Business logic for the Initiator: acts as the Market Data Subscriber.
 *
 * On logon, sends a {@code MarketDataRequest} subscribing to TSLA and NVDA
 * (snapshot + updates, full book, trade entries) and consumes incoming
 * {@code MarketDataIncrementalRefresh} messages, logging each price update.
 */
public class InitiatorApp extends MessageCracker implements Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorApp.class);
    private int receivedUpdates = 0;

    public void onCreate(SessionID sessionID) {
        LOGGER.debug("Session created: {}", sessionID);
    }

    public void onLogon(SessionID sessionID) {
        LOGGER.info("Successfully connected to server: {}", sessionID);

        try {
            // Wait until the FIX session is fully established before sending business messages.
            Thread.sleep(2000);

            MarketDataRequest request = createMarketDataRequest();
            Session.sendToTarget(request, sessionID);
            LOGGER.info("Market data request sent for TSLA and NVDA");
        } catch (Exception e) {
            LOGGER.error("Error sending market data request", e);
        }
    }

    public void onLogout(SessionID sessionID) {
        LOGGER.debug("Disconnected from server: {}", sessionID);
    }

    public void toAdmin(Message message, SessionID sessionID) {
        LOGGER.debug("Sending admin message: {}", message.getClass().getSimpleName());
    }

    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        LOGGER.debug("Received admin message: {}", message.getClass().getSimpleName());
    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        LOGGER.debug("Sending application message: {}", message.getClass().getSimpleName());
    }

    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        LOGGER.info("Received message type: {}", message.getHeader().getString(MsgType.FIELD));
        crack((quickfix.fix50sp2.Message) message, sessionID);
    }

    private MarketDataRequest createMarketDataRequest() {
        MarketDataRequest request = new MarketDataRequest();

        request.set(new MDReqID("TECH_PORTFOLIO_1"));
        request.set(new SubscriptionRequestType('1')); // Snapshot + Updates
        request.set(new MarketDepth(0));               // Full book

        MarketDataRequest.NoMDEntryTypes entryTypes = new MarketDataRequest.NoMDEntryTypes();
        entryTypes.set(new MDEntryType('2'));          // Trade
        request.addGroup(entryTypes);
        request.set(new NoMDEntryTypes(1));

        request.set(new NoRelatedSym(2));

        MarketDataRequest.NoRelatedSym tslaGroup = new MarketDataRequest.NoRelatedSym();
        Instrument tslaInstrument = new Instrument();
        tslaInstrument.set(new Symbol("TSLA"));
        tslaGroup.set(tslaInstrument);
        request.addGroup(tslaGroup);

        MarketDataRequest.NoRelatedSym nvdaGroup = new MarketDataRequest.NoRelatedSym();
        Instrument nvdaInstrument = new Instrument();
        nvdaInstrument.set(new Symbol("NVDA"));
        nvdaGroup.set(nvdaInstrument);
        request.addGroup(nvdaGroup);

        return request;
    }

    @Override
    public void onMessage(MarketDataIncrementalRefresh message, SessionID sessionID)
            throws FieldNotFound {
        int numEntries = message.getGroupCount(NoMDEntries.FIELD);
        LOGGER.info("Received market data update with {} entries", numEntries);

        for (int i = 1; i <= numEntries; i++) {
            MarketDataIncrementalRefresh.NoMDEntries entries = new MarketDataIncrementalRefresh.NoMDEntries();
            message.getGroup(i, entries);

            double price = entries.get(new MDEntryPx()).getValue();
            String symbol = entries.get(new Symbol()).getValue();
            char entryType = entries.get(new MDEntryType()).getValue();
            char updateAction = entries.get(new MDUpdateAction()).getValue();

            LOGGER.info("Market Data Update - Symbol: {}, Price: {}, Type: {}, Action: {}",
                    symbol, price, entryType, updateAction);

            receivedUpdates++;
        }

        if (receivedUpdates % 10 == 0) {
            LOGGER.info("Received {} total market data updates", receivedUpdates);
        }
    }
}
