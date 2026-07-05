package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.response.AiAnalysisResultResponse;
import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct compile-time mapper for transforming {@link AiAnalysisResult} domain
 * entities into {@link AiAnalysisResultResponse} presentation layer DTOs.
 *
 * <p>
 * The {@code riskCategory} field is derived from the numeric {@code riskScore}
 * using an expression mapping, translating the raw integer into a human-readable
 * classification string (LOW, MEDIUM, HIGH) for client-facing API responses.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Mapper(componentModel = "spring")
public interface AiAnalysisResultMapper {

    /**
     * Maps an {@link AiAnalysisResult} entity to an {@link AiAnalysisResultResponse} DTO.
     *
     * <p>
     * The {@code riskCategory} field is computed from the entity's {@code riskScore}:
     * {@code 0–30} maps to LOW, {@code 31–70} maps to MEDIUM, and {@code 71–100}
     * maps to HIGH.
     * </p>
     *
     * @param result the source {@link AiAnalysisResult} entity from the persistence layer
     * @return a populated {@link AiAnalysisResultResponse} ready for API serialization
     */
    @Mapping(target = "riskCategory", expression = "java(resolveRiskCategory(result.getRiskScore()))")
    AiAnalysisResultResponse toResponse(AiAnalysisResult result);

    /**
     * Derives a human-readable risk category label from the provided numerical
     * risk score according to the platform risk classification thresholds.
     *
     * @param riskScore the numerical risk score in the range 0–100
     * @return {@code "LOW"} for 0–30, {@code "MEDIUM"} for 31–70, {@code "HIGH"} for 71–100
     */
    default String resolveRiskCategory(Integer riskScore) {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore <= 30) return "LOW";
        if (riskScore <= 70) return "MEDIUM";
        return "HIGH";
    }
}
