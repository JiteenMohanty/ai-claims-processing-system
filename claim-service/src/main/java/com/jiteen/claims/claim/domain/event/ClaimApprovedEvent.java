package com.jiteen.claims.claim.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event payload published to Kafka when a claim officer formally approves
 * an insurance claim within the Claim Service.
 *
 * <p>
 * The Notification Service consumes this event from {@code claim-approved-topic}
 * to dispatch approval communications (email, SMS) to the claimant, informing them
 * that their claim has been cleared for payment processing.
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
public class ClaimApprovedEvent {

    /**
     * Unique identifier of the approved insurance claim entity.
     */
    private UUID claimId;

    /**
     * Policy number associated with the approved claim, included for
     * contextual reference in notification templates.
     */
    private String policyNumber;

    /**
     * Full name of the claimant to be addressed in notification content.
     */
    private String claimantName;

    /**
     * Identifier of the claim officer or system component that authorized
     * the approval decision.
     */
    private String approvedBy;

    /**
     * Timestamp at which the approval decision was recorded.
     */
    private LocalDateTime approvedAt;
}
