package com.jiteen.claims.claim.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event payload published to Kafka when a claim officer formally rejects
 * an insurance claim within the Claim Service.
 *
 * <p>
 * The Notification Service consumes this event from {@code claim-rejected-topic}
 * to dispatch rejection communications to the claimant, including the documented
 * reason for the denial and any guidance for resubmission or appeal procedures.
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
public class ClaimRejectedEvent {

    /**
     * Unique identifier of the rejected insurance claim entity.
     */
    private UUID claimId;

    /**
     * Policy number associated with the rejected claim, included for
     * contextual reference in notification templates.
     */
    private String policyNumber;

    /**
     * Full name of the claimant to be addressed in notification content.
     */
    private String claimantName;

    /**
     * Identifier of the claim officer or system component that issued
     * the rejection decision.
     */
    private String rejectedBy;

    /**
     * Business justification or administrative reason documenting why the
     * claim was formally denied.
     */
    private String reason;

    /**
     * Timestamp at which the rejection decision was recorded.
     */
    private LocalDateTime rejectedAt;
}
