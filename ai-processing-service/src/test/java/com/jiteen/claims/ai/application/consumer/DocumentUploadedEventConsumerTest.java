package com.jiteen.claims.ai.application.consumer;

import com.jiteen.claims.ai.application.publisher.AiAnalysisEventPublisher;
import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.application.service.OcrService;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link DocumentUploadedEventConsumer}.
 *
 * <p>
 * Validates the orchestration logic: OCR extraction is called, the extracted
 * text is forwarded to the AI analysis service, and the result is published.
 * Error paths are exercised to confirm that exceptions are caught and do not
 * propagate (Kafka would retry indefinitely on uncaught exceptions).
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentUploadedEventConsumer Unit Tests")
class DocumentUploadedEventConsumerTest {

    @Mock
    private OcrService ocrService;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private AiAnalysisEventPublisher aiAnalysisEventPublisher;

    @InjectMocks
    private DocumentUploadedEventConsumer consumer;

    private DocumentUploadedEvent buildEvent() {
        return DocumentUploadedEvent.builder()
                .claimId(UUID.randomUUID())
                .documentId(1L)
                .storagePath("claims/1/doc.pdf")
                .storageProvider("local")
                .contentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should call OCR, then AI analysis, then publish result for valid event")
    void shouldExecuteFullPipelineForValidEvent() {
        // Given
        DocumentUploadedEvent event = buildEvent();
        String ocrText = "Insurance claim policy document content.";
        AiAnalysisCompletedEvent analysis = AiAnalysisCompletedEvent.builder()
                .claimId(event.getClaimId())
                .riskScore(35)
                .summary("Analysis complete")
                .fraudIndicators(List.of())
                .missingDocuments(List.of())
                .recommendedAction("MANUAL_REVIEW_REQUIRED")
                .processedAt(LocalDateTime.now())
                .build();

        when(ocrService.extractText(event)).thenReturn(ocrText);
        when(aiAnalysisService.analyzeDocument(event, ocrText)).thenReturn(analysis);

        // When
        consumer.handleDocumentUploaded(event);

        // Then
        verify(ocrService).extractText(event);
        verify(aiAnalysisService).analyzeDocument(event, ocrText);
        verify(aiAnalysisEventPublisher).publishAnalysisCompleted(analysis);
    }

    @Test
    @DisplayName("Should not publish result when OCR service throws an exception")
    void shouldNotPublishWhenOcrThrows() {
        // Given
        DocumentUploadedEvent event = buildEvent();
        when(ocrService.extractText(event)).thenThrow(new RuntimeException("Textract unavailable"));

        // When
        consumer.handleDocumentUploaded(event);

        // Then — no downstream calls
        verify(aiAnalysisService, never()).analyzeDocument(any(), anyString());
        verify(aiAnalysisEventPublisher, never()).publishAnalysisCompleted(any());
    }

    @Test
    @DisplayName("Should not publish result when AI analysis service throws an exception")
    void shouldNotPublishWhenAnalysisServiceThrows() {
        // Given
        DocumentUploadedEvent event = buildEvent();
        when(ocrService.extractText(event)).thenReturn("some text");
        when(aiAnalysisService.analyzeDocument(any(), anyString()))
                .thenThrow(new RuntimeException("AI service error"));

        // When
        consumer.handleDocumentUploaded(event);

        // Then
        verify(aiAnalysisEventPublisher, never()).publishAnalysisCompleted(any());
    }

    @Test
    @DisplayName("Should swallow publisher exception and not rethrow")
    void shouldSwallowPublisherException() {
        // Given
        DocumentUploadedEvent event = buildEvent();
        String ocrText = "Valid insurance text.";
        AiAnalysisCompletedEvent analysis = AiAnalysisCompletedEvent.builder()
                .claimId(event.getClaimId())
                .riskScore(20)
                .recommendedAction("APPROVE")
                .processedAt(LocalDateTime.now())
                .build();

        when(ocrService.extractText(event)).thenReturn(ocrText);
        when(aiAnalysisService.analyzeDocument(event, ocrText)).thenReturn(analysis);
        doThrow(new RuntimeException("Kafka send failed")).when(aiAnalysisEventPublisher)
                .publishAnalysisCompleted(any());

        // When / Then — must not throw
        consumer.handleDocumentUploaded(event);
    }
}
