package com.jiteen.claims.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap entry point for the AI Processing Service microservice within the
 * AI-Powered Insurance Claims Processing Platform.
 *
 * <p>
 * This service operates asynchronously, consuming {@code CLAIM_CREATED} events
 * from the Kafka messaging infrastructure, executing AI-powered document analysis,
 * risk scoring, fraud detection, and publishing {@code AI_ANALYSIS_COMPLETED}
 * events back to the Claim Service for lifecycle advancement.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@SpringBootApplication
public class AiProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiProcessingServiceApplication.class, args);
    }
}
