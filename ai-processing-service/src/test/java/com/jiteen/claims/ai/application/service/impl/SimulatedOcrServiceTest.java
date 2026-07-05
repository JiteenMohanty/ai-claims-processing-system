package com.jiteen.claims.ai.application.service.impl;

import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link SimulatedOcrService}.
 *
 * <p>
 * Validates that the simulated OCR service returns non-null, non-blank text
 * for both local and S3-backed documents, and that the returned text contains
 * the expected structural keywords to allow downstream AI analysis to function.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@DisplayName("SimulatedOcrService Unit Tests")
class SimulatedOcrServiceTest {

    private final SimulatedOcrService ocrService = new SimulatedOcrService();

    @Test
    @DisplayName("Should return non-blank text for a local-backed document")
    void shouldReturnNonBlankTextForLocalDocument() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(1L)
                .storagePath("/tmp/claims-test-uploads/doc.pdf")
                .storageProvider("local")
                .contentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .build();

        // When
        String result = ocrService.extractText(event);

        // Then
        assertThat(result).isNotBlank();
        assertThat(result.toLowerCase()).contains("insurance");
        assertThat(result.toLowerCase()).contains("policy");
    }

    @Test
    @DisplayName("Should return non-blank text for an S3-backed document")
    void shouldReturnNonBlankTextForS3Document() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(5L)
                .storagePath("claims/5/document.pdf")
                .storageProvider("s3")
                .contentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .build();

        // When
        String result = ocrService.extractText(event);

        // Then
        assertThat(result).isNotBlank();
        assertThat(result.toLowerCase()).contains("insurance");
    }

    @Test
    @DisplayName("Should handle null contentType and uploadedAt without throwing")
    void shouldHandleNullOptionalFieldsGracefully() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(9L)
                .storagePath("claims/9/doc.pdf")
                .storageProvider("local")
                .build();

        // When / Then — no exception expected
        String result = ocrService.extractText(event);
        assertThat(result).isNotBlank();
    }

    @Test
    @DisplayName("Should include documentId in the extracted text body")
    void shouldIncludeDocumentIdInExtractedText() {
        // Given
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(77L)
                .storageProvider("local")
                .contentType("image/png")
                .uploadedAt(LocalDateTime.now())
                .build();

        // When
        String result = ocrService.extractText(event);

        // Then
        assertThat(result).contains("77");
    }
}
