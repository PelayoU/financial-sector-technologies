package es.uc3m.fintech.lesson5.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the SSL bootstrap logging.
 *
 * The actual TLS plumbing (port, keystore, alias, password) is configured
 * declaratively in {@code application.properties} via the
 * {@code server.ssl.*} properties — Spring Boot's auto-configuration handles
 * the heavy lifting. This class only contributes a small bean whose
 * constructor logs an at-a-glance summary of the SSL configuration on
 * application startup, useful when debugging keystore or certificate paths.
 */
@Configuration
public class SslConfig {

    @Bean
    public SslConfigurationLogger sslConfigurationLogger() {
        return new SslConfigurationLogger();
    }

    public static class SslConfigurationLogger {
        private static final Logger LOGGER = LoggerFactory.getLogger(SslConfigurationLogger.class);

        public SslConfigurationLogger() {
            logSslInfo();
        }

        private void logSslInfo() {
            LOGGER.info("SSL Configuration:");
            LOGGER.info("   - HTTPS enabled: true");
            LOGGER.info("   - Server port: 8443");
            LOGGER.info("   - Certificate: Self-signed (development only)");
            LOGGER.info("   - Access URL: https://localhost:8443");
            LOGGER.warn("Using self-signed certificate - browser will show security warning");
            LOGGER.warn("Click 'Advanced' -> 'Proceed to localhost' to continue");
        }
    }
}
