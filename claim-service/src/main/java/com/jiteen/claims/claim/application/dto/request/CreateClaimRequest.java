package com.jiteen.claims.claim.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * for creating a new insurance claim within the system.
 * <p>
 * This class encapsulates the consumer-provided data required to instantiate and kick off 
 * the claim lifecycle within the AI-Powered Insurance Claims Processing Platform. It applies 
 * strict Jakarta Bean Validation constraints to enforce schema constraints and structural 
 * integrity directly at the application's REST ingress tier, matching the engineering 
 * baseline established in the Authentication microservice.
 * </p>
 *
 * @author Senior Java Architect
 * @version 1.0
 * @since Java 21
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateClaimRequest {

    /**
     * The unique alphanumeric identification code of the active insurance policy 
     * contract under which this specific claim coverage is being sought.
     * <p>
     * Must not be blank and is restricted to a maximum length of 100 characters 
     * to prevent database buffer truncation issues.
     * </p>
     */
    @NotBlank(message = "Policy number is mandatory and cannot be blank")
    @Size(max = 100, message = "Policy number must not exceed 100 characters")
    private String policyNumber;

    /**
     * The full legal name of the claimant insurance holder making the submission 
     * or the designated authorized legal representative.
     * <p>
     * Must not be blank and is limited to a maximum length of 255 characters.
     * </p>
     */
    @NotBlank(message = "Claimant name is mandatory and cannot be blank")
    @Size(max = 255, message = "Claimant name must not exceed 255 characters")
    private String claimantName;

    /**
     * The specific categorization sector of the claim coverage field 
     * (e.g., AUTO, HEALTH, PROPERTY, LIFE) indicating the orchestration routing logic.
     * <p>
     * Must not be blank and is limited to a maximum length of 100 characters.
     * </p>
     */
    @NotBlank(message = "Claim type is mandatory and cannot be blank")
    @Size(max = 100, message = "Claim type must not exceed 100 characters")
    private String claimType;

    /**
     * The absolute chronological point in time specifying exactly when the 
     * underlying insured accident, loss event, or incident occurred.
     * <p>
     * Must be a valid timestamp and cannot be null.
     * </p>
     */
    @NotNull(message = "Incident date is mandatory and cannot be null")
    private LocalDateTime incidentDate;

    /**
     * The total calculated monetary evaluation requested by the claimant for 
     * financial payout adjustment.
     * <p>
     * Must not be null and must represent a positive currency value strictly greater than 0.00.
     * </p>
     */
    @NotNull(message = "Claim amount is mandatory and cannot be null")
    @DecimalMin(value = "0.01")
    private BigDecimal claimAmount;

    /**
     * An optional comprehensive, plaintext narrative describing the incident timeline, 
     * contextual variables, causes, or specific qualitative observations of the loss event.
     * <p>
     * This field is optional but restricted to a maximum threshold of 5000 characters 
     * to manage storage performance parameters safely.
     * </p>
     */
    @Size(max = 5000, message = "Description text narrative must not exceed 5000 characters")
    private String description;
}