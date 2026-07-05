package com.jiteen.claims.claim.application.event;

import com.jiteen.claims.claim.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.claim.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.claim.domain.event.ClaimRejectedEvent;
import com.jiteen.claims.claim.domain.event.DocumentUploadedEvent;

/**
 * Application-layer abstraction for publishing insurance claim lifecycle events
 * onto the asynchronous messaging infrastructure.
 *
 * <p>
 * This interface decouples the Claim Service business logic from the specific
 * messaging technology (Kafka, RabbitMQ, SNS, etc.). The concrete implementation
 * is injected at runtime, allowing the messaging backend to be swapped without
 * modifying any service or controller code.
 * </p>
 *
 * <p>
 * All methods are fire-and-forget by design. The service layer publishes events
 * after the corresponding database transaction commits successfully, ensuring
 * the persistent state and the event stream remain consistent.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface ClaimEventPublisher {

    /**
     * Publishes a {@link ClaimCreatedEvent} to notify downstream consumers
     * (primarily the AI Processing Service) that a new claim has been submitted.
     *
     * @param event the fully populated {@link ClaimCreatedEvent} payload
     */
    void publishClaimCreated(ClaimCreatedEvent event);

    /**
     * Publishes a {@link ClaimApprovedEvent} to notify downstream consumers
     * (primarily the Notification Service) that a claim has been approved.
     *
     * @param event the fully populated {@link ClaimApprovedEvent} payload
     */
    void publishClaimApproved(ClaimApprovedEvent event);

    /**
     * Publishes a {@link ClaimRejectedEvent} to notify downstream consumers
     * (primarily the Notification Service) that a claim has been rejected.
     *
     * @param event the fully populated {@link ClaimRejectedEvent} payload
     */
    void publishClaimRejected(ClaimRejectedEvent event);

    /**
     * Publishes a {@link DocumentUploadedEvent} to notify the AI Processing Service
     * that a new supporting document is available for OCR and analysis.
     *
     * @param event the fully populated {@link DocumentUploadedEvent} payload
     */
    void publishDocumentUploaded(DocumentUploadedEvent event);
}
