package com.jiteen.claims.claim.domain.entity;

import com.jiteen.claims.claim.domain.enums.ClaimStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.EnumType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing an insurance claim record within the AI-Powered
 * Insurance Claims Processing Platform.
 * This class maps directly to the 'claims' relational database table.
 *
 * @author Jiteen
 * @version 1.0
 */
@Entity
@Table(name = "claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Claim extends AuditBaseEntity {
    /**
     * Unique identifier for the specific claim transaction[cite: 92].
     * Uses a UUID format to support distributed microservice coordination and
     * decoupling from serial sequence generation.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * The unique identifying number of the active insurance policy under which this
     * claim is submitted.
     * Establishes the core contract link within the claim lifecycle.
     */
    @Column(name = "policy_number", length = 100, nullable = false)
    private String policyNumber;

    /**
     * Full legal name of the claimant policyholder making the insurance claim
     * submission.
     */
    @Column(name = "claimant_name", length = 255, nullable = false)
    private String claimantName;

    /**
     * Categorization of the claim coverage field (e.g., Auto, Property,
     * Health).
     */
    @Column(name = "claim_type", length = 100, nullable = false)
    private String claimType;

    /**
     * Date and time of the incident.
     */
    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    /**
     * Total calculated monetary financial compensation requested by the
     * claimant.
     * Configured with precision 15 and scale 2 to maintain exact enterprise
     * currency accuracy without floating-point errors.
     */
    @Column(name = "claim_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal claimAmount;

    /**
     * State machine workflow indicator tracking progress (e.g., DRAFT, SUBMITTED,
     * APPROVED, REJECTED).
     * Persisted as a plain string representation corresponding to business rule
     * constraints.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;

    /**
     * Comprehensive contextual text description detailing the scenario, damage, or
     * incident specifics.
     * Maps to a database text object for variable extended narratives.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

}