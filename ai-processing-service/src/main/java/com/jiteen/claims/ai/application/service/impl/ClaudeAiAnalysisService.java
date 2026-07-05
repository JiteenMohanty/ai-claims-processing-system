package com.jiteen.claims.ai.application.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Anthropic Claude-backed implementation of the {@link AiAnalysisService}.
 *
 * <p>
 * Activated when {@code ai.provider=claude}. It replaces the rule-based
 * {@link AiAnalysisServiceImpl} simulation with genuine large-language-model
 * analysis: each claim (and each uploaded document) is sent to Claude, which
 * returns a structured risk assessment — a 0–100 risk score, a narrative
 * summary, detected fraud indicators, an officer recommendation, and any
 * missing documents. The response shape is guaranteed by Anthropic's
 * structured-outputs feature (see {@link LlmAnalysisResult}).
 * </p>
 *
 * <p>
 * <strong>Resilience:</strong> this service is deliberately non-fatal. If the
 * Claude call fails for any reason (network error, rate limit, missing
 * credentials, malformed response), it transparently delegates to the
 * simulated {@link AiAnalysisServiceImpl} so the Kafka analysis pipeline always
 * produces a result and never blocks claim processing.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "claude")
public class ClaudeAiAnalysisService implements AiAnalysisService {

    private static final long MAX_TOKENS = 1024L;
    private static final int MAX_DOCUMENT_TEXT_CHARS = 12_000;

    private static final String SYSTEM_PROMPT = """
            You are an expert insurance claims fraud-analysis assistant.
            Assess the supplied claim for fraud risk and completeness, then return a
            structured decision. Follow these rules exactly:
            - riskScore: an integer from 0 (no risk) to 100 (extreme risk).
            - recommendedAction: exactly one of APPROVE, REJECT,
              MANUAL_REVIEW_REQUIRED, or REQUEST_ADDITIONAL_INFORMATION.
            - fraudIndicators: concrete red flags you detected; use an empty list if none.
            - missingDocuments: supporting documents typically required for this claim
              type that appear to be absent; use an empty list if none.
            - summary: two or three sentences a claims officer can act on.
            Be objective and evidence-based. Do not invent facts that are not supported
            by the provided details.
            """;

    private final AnthropicClient anthropicClient;
    private final AiAnalysisServiceImpl fallback;
    private final String model;

    public ClaudeAiAnalysisService(AnthropicClient anthropicClient,
                                   AiAnalysisServiceImpl fallback,
                                   @Value("${ai.claude.model:claude-opus-4-8}") String model) {
        this.anthropicClient = anthropicClient;
        this.fallback = fallback;
        this.model = model;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends the structured claim metadata to Claude for an initial risk
     * assessment. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeCliam(ClaimCreatedEvent claimEvent) {
        log.info("Starting Claude AI analysis for claimId: {} (model={})", claimEvent.getClaimId(), model);

        try {
            String userPrompt = """
                    Analyze the following newly submitted insurance claim (metadata only,
                    no supporting documents have been provided yet):

                    Policy number: %s
                    Claim type: %s
                    Claimant name: %s
                    Submitted at: %s
                    """.formatted(
                    nullSafe(claimEvent.getPolicyNumber()),
                    nullSafe(claimEvent.getClaimType()),
                    nullSafe(claimEvent.getClaimantName()),
                    claimEvent.getSubmittedAt());

            LlmAnalysisResult result = callClaude(userPrompt);

            log.info("Claude analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
                    claimEvent.getClaimId(), result.riskScore(), result.recommendedAction());

            return AiAnalysisCompletedEvent.builder()
                    .claimId(claimEvent.getClaimId())
                    .riskScore(clampScore(result.riskScore()))
                    .summary(result.summary())
                    .fraudIndicators(nullSafeList(result.fraudIndicators()))
                    .recommendedAction(result.recommendedAction())
                    .policyNumberExtracted(claimEvent.getPolicyNumber())
                    .customerNameExtracted(claimEvent.getClaimantName())
                    .missingDocuments(nullSafeList(result.missingDocuments()))
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (Exception ex) {
            log.warn("Claude analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    claimEvent.getClaimId(), ex.getMessage());
            return fallback.analyzeCliam(claimEvent);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enriches the analysis with OCR-extracted document text before sending it
     * to Claude. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeDocument(DocumentUploadedEvent event, String extractedText) {
        log.info("Starting Claude document analysis for claimId: {}, documentId: {} (model={})",
                event.getClaimId(), event.getDocumentId(), model);

        try {
            String trimmedText = truncate(extractedText);
            String userPrompt = """
                    Analyze the following supporting document uploaded for an insurance claim.

                    Claim ID: %s
                    Document ID: %s
                    Original file name: %s
                    Content type: %s

                    OCR-extracted document text:
                    ---
                    %s
                    ---
                    """.formatted(
                    event.getClaimId(),
                    event.getDocumentId(),
                    nullSafe(event.getOriginalFileName()),
                    nullSafe(event.getContentType()),
                    trimmedText == null || trimmedText.isBlank() ? "(no extractable text)" : trimmedText);

            LlmAnalysisResult result = callClaude(userPrompt);

            log.info("Claude document analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
                    event.getClaimId(), result.riskScore(), result.recommendedAction());

            return AiAnalysisCompletedEvent.builder()
                    .claimId(event.getClaimId())
                    .riskScore(clampScore(result.riskScore()))
                    .summary(result.summary())
                    .fraudIndicators(nullSafeList(result.fraudIndicators()))
                    .recommendedAction(result.recommendedAction())
                    .missingDocuments(nullSafeList(result.missingDocuments()))
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (Exception ex) {
            log.warn("Claude document analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    event.getClaimId(), ex.getMessage());
            return fallback.analyzeDocument(event, extractedText);
        }
    }

    /**
     * Issues a single structured-output request to Claude and extracts the typed result.
     *
     * @param userPrompt the fully rendered user-turn prompt describing the claim/document
     * @return the parsed {@link LlmAnalysisResult}
     * @throws IllegalStateException if the model returns no structured content block
     */
    private LlmAnalysisResult callClaude(String userPrompt) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(MAX_TOKENS)
                .system(SYSTEM_PROMPT)
                .addUserMessage(userPrompt);

        var params = builder.outputConfig(LlmAnalysisResult.class).build();

        return anthropicClient.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .map(typed -> typed.text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Claude response contained no structured analysis content"));
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private static List<String> nullSafeList(List<String> list) {
        return list != null ? list : List.of();
    }

    private static String nullSafe(String value) {
        return value != null ? value : "N/A";
    }

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > MAX_DOCUMENT_TEXT_CHARS
                ? text.substring(0, MAX_DOCUMENT_TEXT_CHARS)
                : text;
    }
}
