package es.uc3m.fintech.lesson10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * Bootstraps the FIX Initiator (client side of the session).
 *
 * Loads {@code initiator.cfg} from the classpath, builds the QuickFIX/J
 * runtime and starts a {@link SocketInitiator} that connects to the
 * Acceptor and hosts the {@link InitiatorApp} business logic.
 */
public class InitiatorRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorRunner.class);
    private static SessionSettings settings;

    public static void main(String[] args) {
        URL initiatorConfig = InitiatorRunner.class.getClassLoader().getResource("initiator.cfg");

        Application application = new InitiatorApp();

        try {
            File initiatorConfigFile = new File(initiatorConfig.toURI());
            try (FileInputStream in = new FileInputStream(initiatorConfigFile)) {
                settings = new SessionSettings(in);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading initiator configuration", e);
            return;
        }

        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        Initiator initiator = null;
        try {
            initiator = new SocketInitiator(
                    application, storeFactory, settings, logFactory, messageFactory);
            initiator.start();
            LOGGER.info("Initiator started successfully");

            Thread.currentThread().join();
        } catch (ConfigError | InterruptedException e) {
            LOGGER.error("Error starting initiator", e);
        } finally {
            if (initiator != null) {
                initiator.stop(true);
            }
        }
    }

    public static SessionSettings getSettings() {
        return settings;
    }
}
