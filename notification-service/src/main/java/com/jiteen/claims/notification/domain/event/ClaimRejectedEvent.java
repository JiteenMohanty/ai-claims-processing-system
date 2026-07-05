package com.jiteen.claims.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event payload consumed from the Claim Service when an insurance claim
 * has been formally rejected.
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

    private UUID claimId;
    private String policyNumber;
    private String claimantName;
    private String rejectedBy;
    private String reason;
    private LocalDateTime rejectedAt;
}
