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
 * Groq-backed implementation of the {@link AiAnalysisService}.
 *
 * <p>
 * Activated when {@code ai.provider=groq}. It calls Groq's free, OpenAI-compatible
 * {@code /chat/completions} API (fast open models such as Llama 3.3) in JSON mode,
 * so each claim (and each uploaded document) receives a genuine LLM risk assessment
 * — a 0–100 risk score, a narrative summary, detected fraud indicators, an officer
 * recommendation, and any missing documents.
 * </p>
 *
 * <p>
 * <strong>Resilience:</strong> like the other real backends, this service is
 * deliberately non-fatal. If the Groq call fails for any reason (network error,
 * rate limit, malformed response), it transparently delegates to the simulated
 * {@link AiAnalysisServiceImpl} so the Kafka analysis pipeline always produces a
 * result.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq")
public class GroqAiAnalysisService implements AiAnalysisService {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_DOCUMENT_TEXT_CHARS = 12_000;

    /**
     * System prompt. Because Groq JSON mode does not enforce a schema, the exact
     * JSON contract is spelled out here (and the literal word "json" is required
     * by the API when {@code response_format=json_object}).
     */
    private static final String SYSTEM_PROMPT = """
            You are an expert insurance claims fraud-analysis assistant.
            Assess the supplied claim for fraud risk and completeness, then respond with
            ONLY a single JSON object (no markdown, no prose) with exactly these keys:
              - "riskScore": integer from 0 (no risk) to 100 (extreme risk)
              - "summary": string, two or three sentences a claims officer can act on
              - "fraudIndicators": array of strings (concrete red flags; empty array if none)
              - "recommendedAction": string, exactly one of "APPROVE", "REJECT",
                 "MANUAL_REVIEW_REQUIRED", or "REQUEST_ADDITIONAL_INFORMATION"
              - "missingDocuments": array of strings (documents typically required for this
                 claim type that appear to be absent; empty array if none)
            Be objective and evidence-based. Do not invent facts not supported by the details.
            """;

    private final AiAnalysisServiceImpl fallback;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GroqAiAnalysisService(AiAnalysisServiceImpl fallback,
                                 ObjectMapper objectMapper,
                                 @Value("${GROQ_API_KEY:}") String apiKey,
                                 @Value("${ai.groq.model:llama-3.3-70b-versatile}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AI_PROVIDER=groq requires GROQ_API_KEY to be set. "
                    + "Create a free key at https://console.groq.com/keys, add it to the "
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
     * <p>Sends the structured claim metadata to Groq for an initial risk
     * assessment. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeCliam(ClaimCreatedEvent claimEvent) {
        log.info("Starting Groq AI analysis for claimId: {} (model={})", claimEvent.getClaimId(), model);

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

            LlmAnalysisResult result = callGroq(userPrompt);

            log.info("Groq analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
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
            log.warn("Groq analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    claimEvent.getClaimId(), ex.getMessage());
            return fallback.analyzeCliam(claimEvent);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enriches the analysis with OCR-extracted document text before sending it
     * to Groq. Falls back to simulated analysis on any failure.</p>
     */
    @Override
    public AiAnalysisCompletedEvent analyzeDocument(DocumentUploadedEvent event, String extractedText) {
        log.info("Starting Groq document analysis for claimId: {}, documentId: {} (model={})",
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

            LlmAnalysisResult result = callGroq(userPrompt);

            log.info("Groq document analysis complete for claimId: {} — riskScore: {}, recommendation: {}",
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
            log.warn("Groq document analysis failed for claimId: {} — falling back to simulated analysis. Reason: {}",
                    event.getClaimId(), ex.getMessage());
            return fallback.analyzeDocument(event, extractedText);
        }
    }

    /**
     * Issues a single JSON-mode chat completion to Groq and parses the result.
     *
     * @param userPrompt the fully rendered user-turn prompt describing the claim/document
     * @return the parsed {@link LlmAnalysisResult}
     * @throws Exception if the request fails or the response cannot be parsed
     */
    private LlmAnalysisResult callGroq(String userPrompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)));

        JsonNode response = restClient.post()
                .uri(ENDPOINT)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Groq returned an empty response");
        }

        JsonNode contentNode = response.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new IllegalStateException("Groq response contained no analysis content: " + response);
        }

        return objectMapper.readValue(contentNode.asText(), LlmAnalysisResult.class);
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
