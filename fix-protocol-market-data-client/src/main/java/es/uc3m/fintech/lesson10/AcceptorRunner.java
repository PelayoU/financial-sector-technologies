package es.uc3m.fintech.lesson10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * Bootstraps the FIX Acceptor (server side of the session).
 *
 * Loads {@code acceptor.cfg} from the classpath, builds the QuickFIX/J
 * runtime (store, log, message factories) and starts a {@link SocketAcceptor}
 * that hosts a {@link AcceptorApp} backed by a {@link MarketDataService}.
 */
public class AcceptorRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorRunner.class);
    private static SessionSettings settings;

    public static void main(String[] args) {
        URL acceptorConfig = AcceptorRunner.class.getClassLoader().getResource("acceptor.cfg");

        MarketDataService marketDataService = new MarketDataService();
        marketDataService.init();

        Application application = new AcceptorApp(marketDataService);

        try {
            File acceptorConfigFile = new File(acceptorConfig.toURI());
            try (FileInputStream in = new FileInputStream(acceptorConfigFile)) {
                settings = new SessionSettings(in);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading acceptor configuration", e);
            return;
        }

        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        Acceptor acceptor = null;
        try {
            acceptor = new SocketAcceptor(
                    application, storeFactory, settings, logFactory, messageFactory);
            acceptor.start();
            LOGGER.info("Acceptor started successfully");

            Thread.currentThread().join();
        } catch (ConfigError | InterruptedException e) {
            LOGGER.error("Error starting acceptor", e);
        } finally {
            if (acceptor != null) {
                acceptor.stop(true);
            }
        }
    }

    public static SessionSettings getSettings() {
        return settings;
    }
}
