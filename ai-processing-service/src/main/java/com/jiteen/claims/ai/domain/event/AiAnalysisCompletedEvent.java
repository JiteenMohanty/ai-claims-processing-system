package com.jiteen.claims.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain event payload published to the Claim Service upon completion of
 * AI-powered analysis for an insurance claim.
 *
 * <p>
 * This event is produced by the AI Processing Service to the
 * {@code ai-analysis-completed-topic} after completing the full analysis
 * workflow including OCR extraction, risk scoring, fraud detection, and
 * recommendation generation.
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
     * Unique identifier of the insurance claim that was analysed.
     */
    private UUID claimId;

    /**
     * Numerical risk assessment score on a scale of 0 to 100.
     */
    private int riskScore;

    /**
     * AI-generated concise narrative summary of the claim for officer review.
     */
    private String summary;

    /**
     * Ordered list of detected fraud indicator descriptions.
     */
    private List<String> fraudIndicators;

    /**
     * AI-generated processing recommendation for claim officers.
     * Values: APPROVE, REJECT, MANUAL_REVIEW_REQUIRED, REQUEST_ADDITIONAL_INFORMATION.
     */
    private String recommendedAction;

    /**
     * Policy number extracted from claim documents via OCR analysis.
     */
    private String policyNumberExtracted;

    /**
     * Customer name extracted from supporting documentation.
     */
    private String customerNameExtracted;

    /**
     * List of document types identified as missing during validation.
     */
    private List<String> missingDocuments;

    /**
     * Timestamp at which analysis was completed.
     */
    private LocalDateTime processedAt;
}
