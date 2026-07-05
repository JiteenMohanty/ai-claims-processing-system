package com.jiteen.claims.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event payload consumed from the Claim Service when a new insurance
 * claim is submitted and requires AI analysis.
 *
 * <p>
 * This event is published by the Claim Service to the {@code claim-created-topic}
 * and consumed by this service as the trigger for the automated analysis workflow.
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
     * Unique identifier of the newly submitted insurance claim.
     */
    private UUID claimId;

    /**
     * Policy number associated with the claim.
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
