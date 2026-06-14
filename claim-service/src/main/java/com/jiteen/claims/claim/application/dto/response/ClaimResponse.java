package com.jiteen.claims.claim.application.dto.response;

import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing the outgoing REST API response payload 
 * for an insurance claim record.
 * <p>
 * This class serves as the public API contract for delivering comprehensive claim data 
 * to client applications, downstream services, or API gateways. It isolates internal 
 * infrastructure and persistence mechanics (such as soft-delete tracking fields) 
 * from the external presentation tier, adhering to the enterprise-grade separation of 
 * concerns demonstrated in the Authentication microservice.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimResponse {

    /**
     * The unique identity tracking reference of the insurance claim record.
     */
    private UUID id;

    /**
     * The unique alphanumeric identification code of the associated policy contract 
     * under which coverage is being requested.
     */
    private String policyNumber;

    /**
     * The full legal name of the claimant policyholder or authorized representative 
     * who initiated the submission.
     */
    private String claimantName;

    /**
     * The specific categorization segment of the claim coverage domain 
     * (e.g., AUTO, HEALTH, PROPERTY).
     */
    private String claimType;

    /**
     * The precise timestamp indicating when the underlying accident or loss 
     * incident actually transpired.
     */
    private LocalDateTime incidentDate;

    /**
     * The total calculated monetary amount requested by the claimant for 
     * evaluation and payout adjustment.
     */
    private BigDecimal claimAmount;

    /**
     * The current lifecycle state machine status of the claim within the 
     * automated and manual processing workflow pipeline.
     */
    private ClaimStatus status;

    /**
     * The contextual plaintext narrative detailing specific circumstances, 
     * observations, or explanations surrounding the claim submission.
     */
    private String description;

    /**
     * System audit timestamp denoting exactly when the claim entry was 
     * initially registered within the system of record.
     */
    private LocalDateTime createdAt;

    /**
     * System audit timestamp tracking the absolute last chronological 
     * modification applied to this claim record.
     */
    private LocalDateTime updatedAt;
}