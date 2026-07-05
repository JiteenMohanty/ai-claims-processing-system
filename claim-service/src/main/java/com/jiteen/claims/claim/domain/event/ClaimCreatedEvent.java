package com.jiteen.claims.claim.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event payload published to Kafka when a new insurance claim is successfully
 * created and persisted within the Claim Service.
 *
 * <p>
 * This event initiates the asynchronous AI analysis pipeline. The AI Processing
 * Service consumes this event from {@code claim-created-topic} and begins the
 * automated document analysis, risk scoring, and fraud detection workflow.
 * </p>
 *
 * <p>
 * The payload is intentionally minimal, carrying only the fields required
 * for the downstream consumer to locate the claim and begin processing.
 * Sensitive financial data is not included in the event; the AI Processing
 * Service retrieves full claim details via the Claim Service API if required.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimCreatedEvent {

    /**
     * Unique identifier of the newly created insurance claim entity.
     */
    private UUID claimId;

    /**
     * Policy number associated with the claim, used as a human-readable
     * identifier across service boundaries.
     */
    private String policyNumber;

    /**
     * Categorization of the claim (e.g., AUTO, HEALTH, PROPERTY).
     */
    private String claimType;

    /**
     * Name of the claimant as submitted on the claim request.
     */
    private String claimantName;

    /**
     * Timestamp at which the claim was submitted and persisted.
     */
    private LocalDateTime submittedAt;
}
