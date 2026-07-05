package com.jiteen.claims.ai.application.service.impl;

import java.util.List;

/**
 * Provider-neutral structured result of an LLM-generated insurance claim analysis.
 *
 * <p>
 * Both the Claude and Gemini analysis backends constrain the model to return
 * exactly this JSON shape (via structured outputs / response schemas). Each
 * component maps directly to a field on the outbound
 * {@link com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent}.
 * </p>
 *
 * @param riskScore         numeric fraud/risk assessment on a 0–100 scale
 * @param summary           concise narrative summary for claim-officer review
 * @param fraudIndicators   detected fraud indicator descriptions (empty if none)
 * @param recommendedAction one of {@code APPROVE}, {@code REJECT},
 *                          {@code MANUAL_REVIEW_REQUIRED},
 *                          {@code REQUEST_ADDITIONAL_INFORMATION}
 * @param missingDocuments  document types identified as missing (empty if none)
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public record LlmAnalysisResult(
        int riskScore,
        String summary,
        List<String> fraudIndicators,
        String recommendedAction,
        List<String> missingDocuments
) {
}
