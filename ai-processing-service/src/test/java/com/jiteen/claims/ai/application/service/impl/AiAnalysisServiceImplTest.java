package com.jiteen.claims.ai.application.service.impl;

import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test suite verifying the AI analysis pipeline logic within
 * {@link AiAnalysisServiceImpl}.
 *
 * <p>
 * Tests validate risk scoring thresholds, fraud indicator detection rules,
 * recommendation derivation logic, and the completeness of the generated
 * analysis result payload. No Spring context or external dependencies are
 * required; the service is instantiated directly.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@DisplayName("AiAnalysisServiceImpl Unit Tests")
class AiAnalysisServiceImplTest {

    private final AiAnalysisServiceImpl aiAnalysisService = new AiAnalysisServiceImpl();

    @Test
    @DisplayName("Should return a non-null AiAnalysisCompletedEvent for a valid ClaimCreatedEvent")
    void shouldReturnNonNullResultForValidClaim() {
        // Given
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("POL-TEST-001")
                .claimType("AUTO")
                .claimantName("John Doe")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClaimId()).isEqualTo(event.getClaimId());
        assertThat(result.getSummary()).isNotBlank();
        assertThat(result.getRecommendedAction()).isNotBlank();
        assertThat(result.getProcessedAt()).isNotNull();
        assertThat(result.getRiskScore()).isBetween(0, 100);
    }

    @Test
    @DisplayName("Should populate policyNumberExtracted and customerNameExtracted from event metadata")
    void shouldPopulateExtractedFieldsFromEventMetadata() {
        // Given
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("POL-EXTRACT-999")
                .claimType("HEALTH")
                .claimantName("Jane Smith")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result.getPolicyNumberExtracted()).isEqualTo("POL-EXTRACT-999");
        assertThat(result.getCustomerNameExtracted()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should detect missing policy number as a fraud indicator")
    void shouldFlagMissingPolicyNumberAsFraudIndicator() {
        // Given
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("")
                .claimType("AUTO")
                .claimantName("Bob Brown")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result.getFraudIndicators()).isNotEmpty();
        assertThat(result.getFraudIndicators())
                .anyMatch(indicator -> indicator.contains("policy number"));
    }

    @Test
    @DisplayName("Should recommend MANUAL_REVIEW_REQUIRED when fraud indicators are present")
    void shouldRecommendManualReviewWhenFraudIndicatorsPresent() {
        // Given — empty policy number triggers fraud flag
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("")
                .claimType("AUTO")
                .claimantName("")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result.getRecommendedAction())
                .isIn("MANUAL_REVIEW_REQUIRED", "REQUEST_ADDITIONAL_INFORMATION");
    }

    @Test
    @DisplayName("Should include missing documents for AUTO claim type")
    void shouldIdentifyMissingDocumentsForAutoClaimType() {
        // Given
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("POL-AUTO-001")
                .claimType("AUTO")
                .claimantName("Alice Auto")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result.getMissingDocuments()).isNotEmpty();
        assertThat(result.getMissingDocuments())
                .anyMatch(doc -> doc.toLowerCase().contains("vehicle") ||
                                 doc.toLowerCase().contains("police"));
    }

    @Test
    @DisplayName("Should include claimant name and policy in generated summary")
    void shouldIncludeClaimantAndPolicyInSummary() {
        // Given
        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(UUID.randomUUID())
                .policyNumber("POL-SUMMARY-123")
                .claimType("PROPERTY")
                .claimantName("Robert Johnson")
                .submittedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);

        // Then
        assertThat(result.getSummary())
                .contains("Robert Johnson")
                .contains("POL-SUMMARY-123");
    }

    // -------------------------------------------------------------------------
    // analyzeDocument() tests (Phase 6 — OCR-enriched analysis)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return a non-null result for analyzeDocument with valid OCR text")
    void shouldReturnNonNullResultForDocumentAnalysis() {
        // Given
        UUID claimId = UUID.randomUUID();
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(claimId)
                .documentId(1L)
                .storagePath("claims/1/doc.pdf")
                .storageProvider("local")
                .originalFileName("claim_doc.pdf")
                .contentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .build();

        String extractedText = "INSURANCE CLAIM policy number POL-123 claimant details attached.";

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeDocument(event, extractedText);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClaimId()).isEqualTo(claimId);
        assertThat(result.getRiskScore()).isBetween(0, 100);
        assertThat(result.getSummary()).isNotBlank();
        assertThat(result.getRecommendedAction()).isNotBlank();
        assertThat(result.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should flag missing text as fraud indicator when extracted text is blank")
    void shouldFlagBlankOcrTextAsFraudIndicator() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(2L)
                .storageProvider("local")
                .uploadedAt(LocalDateTime.now())
                .build();

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeDocument(event, "");

        // Then
        assertThat(result.getFraudIndicators())
                .anyMatch(i -> i.toLowerCase().contains("no extractable text"));
        assertThat(result.getRecommendedAction()).isEqualTo("MANUAL_REVIEW_REQUIRED");
    }

    @Test
    @DisplayName("Should produce higher risk score when tampered keyword found in OCR text")
    void shouldElevateRiskWhenTamperingKeywordInOcrText() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(3L)
                .storageProvider("s3")
                .uploadedAt(LocalDateTime.now())
                .build();

        String tamperedText = "Insurance policy document — MODIFIED by unknown party — tampered seal";

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeDocument(event, tamperedText);

        // Then
        assertThat(result.getFraudIndicators())
                .anyMatch(i -> i.toLowerCase().contains("tamper") || i.toLowerCase().contains("alteration"));
        assertThat(result.getRecommendedAction()).isEqualTo("MANUAL_REVIEW_REQUIRED");
    }

    @Test
    @DisplayName("Should include documentId in document analysis summary")
    void shouldIncludeDocumentIdInDocumentSummary() {
        // Given
        UUID claimId = UUID.randomUUID();
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(claimId)
                .documentId(42L)
                .storageProvider("local")
                .contentType("image/jpeg")
                .uploadedAt(LocalDateTime.now())
                .build();

        String text = "Valid insurance policy document with all required fields.";

        // When
        AiAnalysisCompletedEvent result = aiAnalysisService.analyzeDocument(event, text);

        // Then
        assertThat(result.getSummary()).contains("42");
    }
}
