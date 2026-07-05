package com.jiteen.claims.ai.application.service.impl;

import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulated AI analysis service implementation for the AI Processing Service.
 *
 * <p>
 * This implementation provides a realistic, rule-based simulation of the AI
 * analysis pipeline used in production. It applies deterministic business rules
 * combined with controlled random variance to produce risk scores, fraud indicator
 * detection, narrative summaries, and officer recommendations.
 * </p>
 *
 * <p>
 * <strong>Production Migration Path:</strong> To integrate a real AI provider
 * (Anthropic Claude, OpenAI GPT, AWS Bedrock, etc.), implement a new class
 * that satisfies the {@link AiAnalysisService} interface and configure it as the
 * active Spring bean via a profile or feature flag. No consumer or publisher
 * code needs to change.
 * </p>
 *
 * <h3>Risk Score Calculation Logic</h3>
 * <pre>
 *  Base score:       30
 *  Per fraud flag:  +15
 *  Variance range:  ±10 (simulated AI confidence variability)
 *  Maximum:         100
 * </pre>
 *
 * <h3>Fraud Indicator Detection (Rule-Based)</h3>
 * <ul>
 *   <li>HIGH claim type (FRAUD, THEFT) triggers indicator</li>
 *   <li>Null or empty claimant name triggers missing data indicator</li>
 *   <li>Null or empty policy number triggers indicator</li>
 * </ul>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final int BASE_RISK_SCORE = 30;
    private static final int FRAUD_INDICATOR_WEIGHT = 15;
    private static final int SCORE_VARIANCE_BOUND = 10;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Executes the simulated analysis pipeline for the provided claim event.
     * Processing steps:
     * <ol>
     *   <li>Apply rule-based fraud indicator detection</li>
     *   <li>Calculate composite risk score</li>
     *   <li>Generate narrative summary</li>
     *   <li>Derive officer recommendation based on risk profile</li>
     *   <li>Assemble and return the completed event payload</li>
     * </ol>
     * </p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeCliam(ClaimCreatedEvent claimEvent) {
        log.info("Starting AI analysis pipeline for claimId: {}", claimEvent.getClaimId());

        List<String> fraudIndicators = detectFraudIndicators(claimEvent);
        List<String> missingDocuments = identifyMissingDocuments(claimEvent);
        int riskScore = calculateRiskScore(fraudIndicators, missingDocuments);
        String summary = generateSummary(claimEvent, riskScore, fraudIndicators);
        String recommendedAction = deriveRecommendation(riskScore, fraudIndicators, missingDocuments);

        log.info("AI analysis complete for claimId: {} — riskScore: {}, indicators: {}, recommendation: {}",
                claimEvent.getClaimId(), riskScore, fraudIndicators.size(), recommendedAction);

        return AiAnalysisCompletedEvent.builder()
                .claimId(claimEvent.getClaimId())
                .riskScore(riskScore)
                .summary(summary)
                .fraudIndicators(fraudIndicators)
                .recommendedAction(recommendedAction)
                .policyNumberExtracted(claimEvent.getPolicyNumber())
                .customerNameExtracted(claimEvent.getClaimantName())
                .missingDocuments(missingDocuments)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Executes the document-enriched analysis pipeline. The OCR-extracted text is
     * scanned for suspicious patterns, and the standard risk scoring, summary, and
     * recommendation logic is applied with additional context from the document content.
     * </p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeDocument(DocumentUploadedEvent event, String extractedText) {
        log.info("Starting document-enriched AI analysis for claimId: {}, documentId: {}",
                event.getClaimId(), event.getDocumentId());

        List<String> fraudIndicators = detectFraudIndicatorsFromText(extractedText);
        int riskScore = calculateRiskScore(fraudIndicators, List.of());
        String summary = generateDocumentSummary(event, extractedText, riskScore, fraudIndicators);
        String recommendedAction = deriveRecommendation(riskScore, fraudIndicators, List.of());

        log.info("Document AI analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
                event.getClaimId(), riskScore, recommendedAction);

        return AiAnalysisCompletedEvent.builder()
                .claimId(event.getClaimId())
                .riskScore(riskScore)
                .summary(summary)
                .fraudIndicators(fraudIndicators)
                .recommendedAction(recommendedAction)
                .missingDocuments(List.of())
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Applies rule-based heuristics to identify potential fraud indicators
     * based on the available claim metadata.
     *
     * @param event the claim event containing metadata for evaluation
     * @return a list of detected fraud indicator description strings
     */
    private List<String> detectFraudIndicators(ClaimCreatedEvent event) {
        List<String> indicators = new ArrayList<>();

        if (event.getPolicyNumber() == null || event.getPolicyNumber().isBlank()) {
            indicators.add("Missing or invalid policy number reference");
        }

        if (event.getClaimantName() == null || event.getClaimantName().isBlank()) {
            indicators.add("Missing claimant identity information");
        }

        if (event.getClaimType() != null &&
                (event.getClaimType().toUpperCase().contains("FRAUD") ||
                 event.getClaimType().toUpperCase().contains("THEFT"))) {
            indicators.add("High-risk claim category flagged for enhanced scrutiny");
        }

        return indicators;
    }

    /**
     * Identifies document types that are commonly required but appear to be
     * missing based on the claim type and available metadata.
     *
     * @param event the claim event containing claim type and metadata
     * @return a list of missing document type descriptions
     */
    private List<String> identifyMissingDocuments(ClaimCreatedEvent event) {
        List<String> missing = new ArrayList<>();

        String claimType = event.getClaimType() != null ? event.getClaimType().toUpperCase() : "";

        if (claimType.contains("AUTO") || claimType.contains("VEHICLE")) {
            missing.add("Vehicle damage assessment report");
            missing.add("Police report or incident reference");
        } else if (claimType.contains("HEALTH") || claimType.contains("MEDICAL")) {
            missing.add("Medical diagnosis documentation");
            missing.add("Hospital admission records");
        } else if (claimType.contains("PROPERTY")) {
            missing.add("Property damage photographs");
            missing.add("Independent repair estimate");
        }

        return missing;
    }

    /**
     * Calculates a composite risk score based on detected fraud indicators,
     * missing document count, and controlled random variance to simulate
     * AI model confidence variability.
     *
     * @param fraudIndicators list of detected fraud indicators
     * @param missingDocuments list of identified missing documents
     * @return a risk score in the range 0–100
     */
    private int calculateRiskScore(List<String> fraudIndicators, List<String> missingDocuments) {
        int score = BASE_RISK_SCORE;
        score += fraudIndicators.size() * FRAUD_INDICATOR_WEIGHT;
        score += missingDocuments.size() * 5;

        int variance = ThreadLocalRandom.current().nextInt(-SCORE_VARIANCE_BOUND, SCORE_VARIANCE_BOUND + 1);
        score += variance;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Generates a concise AI narrative summary of the claim for claim officer review.
     *
     * @param event          the claim event metadata
     * @param riskScore      the calculated risk score
     * @param fraudIndicators the detected fraud indicators
     * @return a formatted narrative summary string
     */
    private String generateSummary(ClaimCreatedEvent event, int riskScore, List<String> fraudIndicators) {
        String riskCategory = riskScore <= 30 ? "LOW" : riskScore <= 70 ? "MEDIUM" : "HIGH";

        StringBuilder summary = new StringBuilder();
        summary.append(String.format(
                "Insurance claim submitted by %s under policy %s. Claim type: %s. ",
                event.getClaimantName() != null ? event.getClaimantName() : "Unknown",
                event.getPolicyNumber() != null ? event.getPolicyNumber() : "N/A",
                event.getClaimType() != null ? event.getClaimType() : "Unspecified"));

        summary.append(String.format(
                "AI risk assessment score: %d (%s RISK). ", riskScore, riskCategory));

        if (!fraudIndicators.isEmpty()) {
            summary.append(String.format(
                    "%d fraud indicator(s) detected requiring officer attention. ", fraudIndicators.size()));
        } else {
            summary.append("No fraud indicators detected during automated analysis. ");
        }

        summary.append("Claim is ready for officer review and decision.");

        return summary.toString();
    }

    /**
     * Derives the processing recommendation based on the risk profile, fraud
     * indicators, and missing documentation assessment.
     *
     * @param riskScore        the composite risk score
     * @param fraudIndicators  detected fraud indicators
     * @param missingDocuments identified missing required documents
     * @return a recommendation string for the claim officer
     */
    private String deriveRecommendation(int riskScore, List<String> fraudIndicators,
                                        List<String> missingDocuments) {
        if (!missingDocuments.isEmpty()) {
            return "REQUEST_ADDITIONAL_INFORMATION";
        }
        if (riskScore >= 71 || !fraudIndicators.isEmpty()) {
            return "MANUAL_REVIEW_REQUIRED";
        }
        if (riskScore >= 31) {
            return "MANUAL_REVIEW_REQUIRED";
        }
        return "APPROVE";
    }

    /**
     * Scans OCR-extracted document text for keywords and patterns associated with
     * fraudulent or high-risk claims.
     *
     * @param text the plain text extracted from the document via OCR
     * @return a list of detected fraud indicator description strings
     */
    private List<String> detectFraudIndicatorsFromText(String text) {
        List<String> indicators = new ArrayList<>();

        if (text == null || text.isBlank()) {
            indicators.add("No extractable text found in uploaded document");
            return indicators;
        }

        String lowerText = text.toLowerCase();

        if (!lowerText.contains("policy") && !lowerText.contains("insurance")) {
            indicators.add("Document does not appear to be an insurance-related file");
        }

        if (lowerText.contains("alteration") || lowerText.contains("modified")
                || lowerText.contains("tampered")) {
            indicators.add("Document text contains alteration or tampering references");
        }

        return indicators;
    }

    /**
     * Generates a concise AI summary for a document-enriched analysis result.
     *
     * @param event          the document upload event
     * @param extractedText  the OCR-extracted document text
     * @param riskScore      the calculated risk score
     * @param fraudIndicators detected fraud indicators
     * @return a formatted narrative summary string
     */
    private String generateDocumentSummary(DocumentUploadedEvent event, String extractedText,
                                            int riskScore, List<String> fraudIndicators) {
        String riskCategory = riskScore <= 30 ? "LOW" : riskScore <= 70 ? "MEDIUM" : "HIGH";
        int textLength = extractedText != null ? extractedText.length() : 0;

        StringBuilder summary = new StringBuilder();
        summary.append(String.format(
                "Document analysis for claim %s (documentId: %d). ",
                event.getClaimId(), event.getDocumentId()));
        summary.append(String.format(
                "OCR extracted %d characters of text from the %s document. ",
                textLength, event.getContentType() != null ? event.getContentType() : "unknown"));
        summary.append(String.format(
                "AI risk assessment score: %d (%s RISK). ", riskScore, riskCategory));

        if (!fraudIndicators.isEmpty()) {
            summary.append(String.format(
                    "%d document-level fraud indicator(s) detected. ", fraudIndicators.size()));
        } else {
            summary.append("No document-level fraud indicators detected. ");
        }

        summary.append("Document analysis complete — claim ready for officer review.");
        return summary.toString();
    }
}
