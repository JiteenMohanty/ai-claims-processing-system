package com.jiteen.claims.ai.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini-backed implementation of the {@link AiAnalysisService}.
 *
 * <p>
 * Activated when {@code ai.provider=gemini}. It calls the free-tier Gemini
 * {@code generateContent} REST API with a JSON response schema, so each claim
 * (and each uploaded document) receives a genuine LLM risk assessment — a 0–100
 * risk score, a narrative summary, detected fraud indicators, an officer
 * recommendation, and any missing documents.
 * </p>
 *
 * <p>
 * <strong>Resilience:</strong> like the Claude backend, this service is
 * deliberately non-fatal. If the Gemini call fails for any reason (network
 * error, quota exhaustion, malformed response), it transparently delegates to
 * the simulated {@link AiAnalysisServiceImpl} so the Kafka analysis pipeline
 * always produces a result.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiAiAnalysisService implements AiAnalysisService {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
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

    /** JSON schema constraining Gemini's response to the {@link LlmAnalysisResult} shape. */
    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "riskScore", Map.of("type", "INTEGER"),
                    "summary", Map.of("type", "STRING"),
                    "fraudIndicators", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                    "recommendedAction", Map.of("type", "STRING"),
                    "missingDocuments", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))),
            "required", List.of(
                    "riskScore", "summary", "fraudIndicators", "recommendedAction", "missingDocuments"));

    private final AiAnalysisServiceImpl fallback;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiAnalysisService(AiAnalysisServiceImpl fallback,
                                   ObjectMapper objectMapper,
                                   @Value("${GEMINI_API_KEY:}") String apiKey,
                                   @Value("${ai.gemini.model:gemini-2.0-flash}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AI_PROVIDER=gemini requires GEMINI_API_KEY to be set. "
                    + "Create a free key at https://aistudio.google.com/apikey, add it to the "
                    + "project .env and restart, or set AI_PROVIDER=simulated to use the built-in analysis.");
        }
        this.fallback = fallback;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends the structured claim metadata to Gemini for an initial risk
     * assessment. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeCliam(ClaimCreatedEvent claimEvent) {
        log.info("Starting Gemini AI analysis for claimId: {} (model={})", claimEvent.getClaimId(), model);

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

            LlmAnalysisResult result = callGemini(userPrompt);

            log.info("Gemini analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
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
            log.warn("Gemini analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    claimEvent.getClaimId(), ex.getMessage());
            return fallback.analyzeCliam(claimEvent);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enriches the analysis with OCR-extracted document text before sending it
     * to Gemini. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeDocument(DocumentUploadedEvent event, String extractedText) {
        log.info("Starting Gemini document analysis for claimId: {}, documentId: {} (model={})",
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

            LlmAnalysisResult result = callGemini(userPrompt);

            log.info("Gemini document analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
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
            log.warn("Gemini document analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    event.getClaimId(), ex.getMessage());
            return fallback.analyzeDocument(event, extractedText);
        }
    }

    /**
     * Issues a single structured-output request to the Gemini API and parses the result.
     *
     * @param userPrompt the fully rendered user-turn prompt describing the claim/document
     * @return the parsed {@link LlmAnalysisResult}
     * @throws Exception if the request fails or the response cannot be parsed
     */
    private LlmAnalysisResult callGemini(String userPrompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", RESPONSE_SCHEMA));

        JsonNode response = restClient.post()
                .uri(ENDPOINT, model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Gemini returned an empty response");
        }

        JsonNode textNode = response.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini response contained no analysis content: " + response);
        }

        return objectMapper.readValue(textNode.asText(), LlmAnalysisResult.class);
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
