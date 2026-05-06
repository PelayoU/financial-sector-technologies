package es.uc3m.fintech.lesson5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the secure messaging application.
 *
 * The TLS endpoint is configured via {@code application.properties} —
 * starts on {@code https://localhost:8443} backed by the bundled
 * {@code keystore.p12} (alias {@code lesson5-https}, self-signed).
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
