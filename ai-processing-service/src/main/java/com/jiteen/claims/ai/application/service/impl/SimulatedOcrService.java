package com.jiteen.claims.ai.application.service.impl;

import com.jiteen.claims.ai.application.service.OcrService;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simulated OCR service used when Amazon Textract is not available.
 *
 * <p>
 * In production, this bean would be replaced by a Textract-backed implementation
 * that fetches the document from S3 and calls
 * {@code TextractClient.detectDocumentText()} or
 * {@code TextractClient.analyzeDocument()} depending on the required extraction
 * fidelity. The {@link OcrService} interface ensures zero changes to the AI
 * analysis pipeline when the real implementation is swapped in.
 * </p>
 *
 * <p>
 * <strong>Production Migration Path:</strong> Implement
 * {@code TextractOcrService implements OcrService}, annotate it with
 * {@code @ConditionalOnProperty(name = "ocr.provider", havingValue = "textract")},
 * and annotate this class with
 * {@code @ConditionalOnProperty(name = "ocr.provider", havingValue = "simulated", matchIfMissing = true)}.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
public class SimulatedOcrService implements OcrService {

    /**
     * {@inheritDoc}
     *
     * <p>
     * Logs a simulation notice and returns a synthetic document text body
     * representing what Textract would extract from a real insurance claim document.
     * The simulated text includes policy references, incident details, and claim
     * amounts to enable downstream AI analysis to exercise the full extraction pipeline.
     * </p>
     */
    @Override
    public String extractText(DocumentUploadedEvent event) {
        if ("s3".equalsIgnoreCase(event.getStorageProvider())) {
            log.info("[OCR SIMULATION] Would call Amazon Textract for S3 document: " +
                     "bucket/key={}, claimId={}, documentId={}",
                     event.getStoragePath(), event.getClaimId(), event.getDocumentId());
        } else {
            log.info("[OCR SIMULATION] Local document — Textract not applicable: " +
                     "path={}, claimId={}", event.getStoragePath(), event.getClaimId());
        }

        return buildSimulatedDocumentText(event);
    }

    private String buildSimulatedDocumentText(DocumentUploadedEvent event) {
        return String.format("""
                INSURANCE CLAIM DOCUMENT
                ========================
                Document Reference: %s
                Content Type: %s
                Upload Date: %s

                CLAIM DETAILS EXTRACTED BY OCR:
                Policy Number: [extracted-from-document]
                Claimant Name: [extracted-from-document]
                Incident Date: [extracted-from-document]
                Claim Amount: [extracted-from-document]
                Incident Description: Vehicle involved in rear-end collision at intersection.
                Witness statements available.
                Police report filed. Reference: PR-2024-98765.
                Estimated repair cost: $4,500.00

                DOCUMENT AUTHENTICITY INDICATORS:
                - Document timestamp present
                - Issuing authority signature detected
                - No signs of digital alteration detected
                """,
                event.getDocumentId(),
                event.getContentType() != null ? event.getContentType() : "unknown",
                event.getUploadedAt() != null ? event.getUploadedAt().toLocalDate() : "N/A");
    }
}
