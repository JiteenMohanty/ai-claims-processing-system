package com.jiteen.claims.claim.application.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing the incoming REST API request payload
 * for updating an existing insurance claim within the system.
 * <p>
 * This class captures modifications to an active claim resource. To fully support patch-style 
 * or selective partial updates where consumers only transmit modified values, all fields are 
 * explicitly nullable. Structural integrity constraints and boundary sizing limitations from the 
 * enterprise specification are enforced via Jakarta Bean Validation annotations, executing only 
 * when the respective fields are non-null.
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
public class UpdateClaimRequest {

    /**
     * The updated unique alphanumeric identification code of the insurance policy contract.
     * <p>
     * Field is optional to allow partial updates but is restricted to a maximum length of 100 
     * characters to prevent database truncation errors when provided.
     * </p>
     */
    @Size(max = 100, message = "Policy number must not exceed 100 characters")
    private String policyNumber;

    /**
     * The updated full legal name of the claimant policyholder or designated legal representative.
     * <p>
     * Field is optional to allow partial updates but is restricted to a maximum length of 255 
     * characters when provided.
     * </p>
     */
    @Size(max = 255, message = "Claimant name must not exceed 255 characters")
    private String claimantName;

    /**
     * The updated categorization sector of the claim coverage domain (e.g., AUTO, HEALTH, PROPERTY).
     * <p>
     * Field is optional to allow partial updates but is restricted to a maximum length of 100 
     * characters when provided.
     * </p>
     */
    @Size(max = 100, message = "Claim type must not exceed 100 characters")
    private String claimType;

    /**
     * The updated chronological timestamp specifying exactly when the insured incident transpired.
     * <p>
     * Field is optional to allow partial updates. No validation constraint is applied when null.
     * </p>
     */
    private LocalDateTime incidentDate;

    /**
     * The updated monetary evaluation requested by the claimant for financial settlement.
     * <p>
     * Field is optional to allow partial updates, but when provided it must represent a positive 
     * currency value strictly greater than zero.
     * </p>
     */
    @Positive(message = "Claim amount must be a positive value greater than zero")
    private BigDecimal claimAmount;

    /**
     * The updated plaintext narrative detailing contextual circumstances or structural breakdown 
     * of the loss event.
     * <p>
     * Field is optional and restricted to a maximum threshold of 5000 characters to manage storage 
     * metrics safely.
     * </p>
     */
    @Size(max = 5000, message = "Description text narrative must not exceed 5000 characters")
    private String description;
}