package com.jiteen.claims.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap entry point for the Notification Service microservice within the
 * AI-Powered Insurance Claims Processing Platform.
 *
 * <p>
 * This service consumes {@code CLAIM_APPROVED} and {@code CLAIM_REJECTED} events
 * from the Kafka messaging infrastructure and dispatches customer-facing email
 * notifications communicating the outcome of their insurance claim submission.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
