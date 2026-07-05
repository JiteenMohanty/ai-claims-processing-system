package com.jiteen.claims.claim.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain event payload consumed from Kafka when the AI Processing Service has
 * completed its automated analysis of an insurance claim.
 *
 * <p>
 * This event is produced by the AI Processing Service on the
 * {@code ai-analysis-completed-topic} and consumed by the Claim Service.
 * Upon receipt, the Claim Service persists the structured analysis results
 * to the {@code ai_analysis_results} table and advances the claim lifecycle
 * status to {@code AI_REVIEW_COMPLETED}.
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
public class AiAnalysisCompletedEvent {

    /**
     * Unique identifier of the insurance claim for which AI analysis was performed.
     */
    private UUID claimId;

    /**
     * Numerical risk assessment score on a scale of 0 to 100.
     * {@code 0–30} = Low Risk; {@code 31–70} = Medium Risk; {@code 71–100} = High Risk.
     */
    private int riskScore;

    /**
     * AI-generated concise narrative summarizing the claim context, incident
     * circumstances, and key extracted information for claim officer review.
     */
    private String summary;

    /**
     * Ordered list of detected fraud indicator descriptions identified during
     * automated document analysis and pattern matching routines.
     */
    private List<String> fraudIndicators;

    /**
     * AI-generated processing recommendation for claim officers.
     * Possible values: {@code APPROVE}, {@code REJECT},
     * {@code MANUAL_REVIEW_REQUIRED}, {@code REQUEST_ADDITIONAL_INFORMATION}.
     */
    private String recommendedAction;

    /**
     * Policy number extracted from uploaded claim documents via OCR processing.
     */
    private String policyNumberExtracted;

    /**
     * Full customer name extracted from supporting document data.
     */
    private String customerNameExtracted;

    /**
     * List of identified missing documents required for complete claim processing.
     */
    private List<String> missingDocuments;

    /**
     * Timestamp at which the AI Processing Service completed its analysis workflow.
     */
    private LocalDateTime processedAt;
}
