package com.jiteen.claims.ai.application.publisher;

import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;

/**
 * Application-layer abstraction for publishing AI analysis completion events
 * to the asynchronous messaging infrastructure.
 *
 * <p>
 * This interface decouples the AI analysis business logic from the concrete
 * messaging implementation (Kafka, RabbitMQ, SNS, etc.), allowing the backend
 * to be swapped without modifying the service or consumer layers.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface AiAnalysisEventPublisher {

    /**
     * Publishes an {@link AiAnalysisCompletedEvent} to notify the Claim Service
     * that AI analysis has completed for the referenced insurance claim.
     *
     * @param event the fully populated {@link AiAnalysisCompletedEvent} payload
     */
    void publishAnalysisCompleted(AiAnalysisCompletedEvent event);
}
