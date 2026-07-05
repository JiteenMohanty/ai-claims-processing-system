package com.jiteen.claims.ai.application.service;

import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;

/**
 * Application service contract defining the AI analysis pipeline for insurance
 * claims within the AI Processing Service.
 *
 * <p>
 * Implementations of this interface encapsulate the core intelligence processing
 * workflow: OCR extraction simulation, metadata parsing, risk scoring, fraud
 * detection, summary generation, and recommendation derivation. The interface
 * abstraction allows the underlying AI backend (simulated, OpenAI, AWS Bedrock,
 * Anthropic Claude, etc.) to be swapped without modifying the consumer or
 * publisher layers.
 * </p>
 *
 * <p>
 * Two analysis entry points are provided:
 * <ul>
 *   <li>{@link #analyzeCliam(ClaimCreatedEvent)} — triggered immediately on claim
 *       creation using structured claim metadata only (no document content).</li>
 *   <li>{@link #analyzeDocument(DocumentUploadedEvent, String)} — triggered when a
 *       supporting document is uploaded and OCR text has been extracted, providing
 *       richer analysis input for risk scoring and fraud detection.</li>
 * </ul>
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface AiAnalysisService {

    /**
     * Executes the full AI analysis pipeline for the specified insurance claim
     * using structured claim metadata only.
     *
     * <p>
     * Called immediately after claim creation before any documents are available.
     * Produces an initial risk assessment based on policy number, claim type,
     * claimant identity, and submission timestamp.
     * </p>
     *
     * @param claimEvent the {@link ClaimCreatedEvent} containing claim metadata
     * @return a fully populated {@link AiAnalysisCompletedEvent}
     */
    AiAnalysisCompletedEvent analyzeCliam(ClaimCreatedEvent claimEvent);

    /**
     * Executes the AI analysis pipeline enriched with OCR-extracted document text.
     *
     * <p>
     * Called after a supporting document is uploaded and processed through the OCR
     * pipeline. The extracted text enables the AI model to identify specific
     * metadata (policy number, claim amount, incident date) and detect document-level
     * inconsistencies that are not visible from structured claim metadata alone.
     * </p>
     *
     * @param event         the {@link DocumentUploadedEvent} carrying the claim context
     * @param extractedText plain text extracted from the document by the OCR service
     * @return a fully populated {@link AiAnalysisCompletedEvent} enriched with
     *         document intelligence
     */
    AiAnalysisCompletedEvent analyzeDocument(DocumentUploadedEvent event, String extractedText);
}
