package es.uc3m.fintech.lesson10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MDReqID;
import quickfix.field.MsgType;
import quickfix.fix50sp2.MarketDataRequest;

/**
 * Business logic for the Acceptor: acts as the Market Data Provider.
 *
 * On {@code MarketDataRequest}, extracts the {@code MDReqID} and asks the
 * {@link MarketDataService} to start streaming {@code MarketDataIncrementalRefresh}
 * messages back to the Initiator.
 */
public class AcceptorApp extends MessageCracker implements Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorApp.class);
    private final MarketDataService marketDataService;

    public AcceptorApp(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    public void onCreate(SessionID sessionID) {
        LOGGER.debug("Session created: {}", sessionID);
    }

    public void onLogon(SessionID sessionID) {
        LOGGER.info("Client logged on: {}", sessionID);
    }

    public void onLogout(SessionID sessionID) {
        LOGGER.debug("Client logged out: {}", sessionID);
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
        crack(message, sessionID);
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) {
        try {
            String mdReqId = message.getString(MDReqID.FIELD);
            LOGGER.info("Received market data request: {}", mdReqId);
            this.marketDataService.start(sessionID, mdReqId);
        } catch (Exception e) {
            LOGGER.error("MDReqID not found in market data request", e);
        }
    }
}
