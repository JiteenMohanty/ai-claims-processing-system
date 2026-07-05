package com.jiteen.claims.claim.domain.entity;

import com.jiteen.claims.claim.domain.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity representing the structured results of AI-powered analysis
 * performed on an insurance claim by the AI Processing Service.
 *
 * <p>
 * Each {@link Claim} has at most one associated {@code AiAnalysisResult} record,
 * populated asynchronously when the AI Processing Service publishes an
 * {@code AI_ANALYSIS_COMPLETED} Kafka event. This entity stores all AI-generated
 * intelligence including risk scores, fraud indicators, document summaries, and
 * processing recommendations for claim officer review.
 * </p>
 *
 * <p>
 * Fraud indicators and missing document lists are stored as JSON-encoded
 * TEXT columns using the {@link StringListConverter}, allowing structured
 * list persistence without requiring additional join tables.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Entity
@Table(name = "ai_analysis_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AiAnalysisResult extends AuditBaseEntity {

    /**
     * Unique identifier for this AI analysis result record.
     * Uses UUID for distributed microservice compatibility.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Foreign key reference to the parent insurance claim for which
     * this analysis was performed. Enforced as a unique constraint
     * to ensure one-to-one cardinality per claim.
     */
    @Column(name = "claim_id", nullable = false, unique = true)
    private UUID claimId;

    /**
     * AI-generated concise narrative summarizing the claim context,
     * extracted key facts, and overall assessment for claim officer review.
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * Numerical risk assessment score on a 0–100 scale.
     * {@code 0–30} = Low Risk; {@code 31–70} = Medium Risk; {@code 71–100} = High Risk.
     */
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    /**
     * AI-generated processing recommendation for claim officers.
     * Possible values: {@code APPROVE}, {@code REJECT},
     * {@code MANUAL_REVIEW_REQUIRED}, {@code REQUEST_ADDITIONAL_INFORMATION}.
     */
    @Column(name = "recommended_action", length = 100)
    private String recommendedAction;

    /**
     * JSON-encoded list of fraud indicator descriptions identified during
     * automated document analysis and behavioral pattern matching.
     * Serialized as a JSON array TEXT column via {@link StringListConverter}.
     */
    @Convert(converter = StringListConverter.class)
    @Column(name = "fraud_indicators", columnDefinition = "TEXT")
    private List<String> fraudIndicators;

    /**
     * Policy number extracted from claim documents via OCR processing.
     */
    @Column(name = "policy_number", length = 100)
    private String policyNumber;

    /**
     * Claim amount figure extracted from supporting documentation analysis.
     */
    @Column(name = "claim_amount_extracted")
    private Double claimAmountExtracted;

    /**
     * Incident date extracted from supporting documentation by the AI model.
     */
    @Column(name = "incident_date_extracted")
    private LocalDateTime incidentDateExtracted;

    /**
     * Customer name as extracted from claim documents via OCR analysis.
     */
    @Column(name = "customer_name_extracted", length = 255)
    private String customerNameExtracted;

    /**
     * JSON-encoded list of document types identified as missing or incomplete
     * during the AI document validation pass. Serialized via {@link StringListConverter}.
     */
    @Convert(converter = StringListConverter.class)
    @Column(name = "missing_documents", columnDefinition = "TEXT")
    private List<String> missingDocuments;

    /**
     * Timestamp at which the AI Processing Service completed its analysis
     * and published the result event.
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
