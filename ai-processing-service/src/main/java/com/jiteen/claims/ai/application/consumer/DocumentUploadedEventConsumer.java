package com.jiteen.claims.ai.application.consumer;

import com.jiteen.claims.ai.application.publisher.AiAnalysisEventPublisher;
import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.application.service.OcrService;
import com.jiteen.claims.ai.config.KafkaTopics;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that drives the document-upload-triggered AI analysis pipeline.
 *
 * <p>
 * When the Claim Service uploads a supporting document (PDF, image, etc.) and
 * publishes a {@link DocumentUploadedEvent}, this consumer:
 * <ol>
 *   <li>Extracts machine-readable text from the document via the {@link OcrService}
 *       (Amazon Textract in production, simulation fallback in development).</li>
 *   <li>Submits the extracted text alongside the original event metadata to the
 *       {@link AiAnalysisService} for risk scoring, fraud detection, and summary
 *       generation.</li>
 *   <li>Publishes the resulting {@link AiAnalysisCompletedEvent} back to the Claim
 *       Service via Kafka.</li>
 * </ol>
 * This enriches the analysis with actual document content rather than relying solely
 * on the structured claim metadata delivered in the {@code ClaimCreatedEvent}.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadedEventConsumer {

    private final OcrService ocrService;
    private final AiAnalysisService aiAnalysisService;
    private final AiAnalysisEventPublisher aiAnalysisEventPublisher;

    /**
     * Handles an inbound {@link DocumentUploadedEvent}, executing OCR followed
     * by the full AI analysis pipeline and publishing the result.
     *
     * <p>
     * Processing is intentionally idempotent: if the AI analysis for the given
     * {@code claimId} has already been persisted by the Claim Service, the
     * duplicate result will be silently skipped by the consumer on the other end.
     * </p>
     *
     * @param event the {@link DocumentUploadedEvent} payload received from the
     *              {@value KafkaTopics#DOCUMENT_UPLOADED} topic
     */
    @KafkaListener(
            topics = KafkaTopics.DOCUMENT_UPLOADED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "documentUploadedListenerContainerFactory"
    )
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Document upload event received — claimId: {}, documentId: {}, provider: {}",
                event.getClaimId(), event.getDocumentId(), event.getStorageProvider());

        try {
            // Step 1: Extract text via OCR (Textract or simulation)
            String extractedText = ocrService.extractText(event);
            log.debug("OCR extraction complete for documentId: {} — extracted {} characters",
                    event.getDocumentId(), extractedText != null ? extractedText.length() : 0);

            // Step 2: Run AI analysis with the enriched document text
            AiAnalysisCompletedEvent result =
                    aiAnalysisService.analyzeDocument(event, extractedText);

            // Step 3: Publish result for the Claim Service to persist and act on
            aiAnalysisEventPublisher.publishAnalysisCompleted(result);

            log.info("Document AI analysis complete — claimId: {}, riskScore: {}, recommendation: {}",
                    event.getClaimId(), result.getRiskScore(), result.getRecommendedAction());

        } catch (Exception ex) {
            log.error("Failed to process DocumentUploadedEvent for claimId: {}, documentId: {} — {}",
                    event.getClaimId(), event.getDocumentId(), ex.getMessage(), ex);
        }
    }
}
