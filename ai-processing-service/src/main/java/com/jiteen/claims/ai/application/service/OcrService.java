package com.jiteen.claims.ai.application.service;

import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;

/**
 * Application service contract defining the OCR (Optical Character Recognition)
 * capability for extracting machine-readable text from uploaded claim documents.
 *
 * <p>
 * The Version 1 production implementation uses Amazon Textract to process PDF
 * and image documents stored in AWS S3, returning structured plain text ready
 * for downstream AI analysis. A {@code SimulatedOcrService} is provided as a
 * local-development fallback when AWS credentials or S3 are not configured.
 * </p>
 *
 * <p>
 * The abstraction allows the OCR backend to be replaced (e.g., Google Document AI,
 * Azure Form Recognizer) without changing the AI analysis pipeline.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface OcrService {

    /**
     * Extracts machine-readable text from the document referenced by the given
     * upload event.
     *
     * <p>
     * Implementations may call Amazon Textract for S3-backed documents, or return
     * a simulated placeholder string when the document is stored locally or when
     * AWS credentials are unavailable.
     * </p>
     *
     * @param event the {@link DocumentUploadedEvent} carrying the storage path and
     *              storage provider details needed to locate the binary asset
     * @return extracted plain text from the document, or a simulation placeholder
     *         if OCR is unavailable
     */
    String extractText(DocumentUploadedEvent event);
}
