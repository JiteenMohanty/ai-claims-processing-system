package com.jiteen.claims.claim.application.service;

import com.jiteen.claims.claim.application.dto.response.DocumentResponse;
import com.jiteen.claims.claim.application.dto.response.UploadDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service layer interface defining the core business contract for managing insurance 
 * claim attachments and supporting documents within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This contract orchestrates the multi-part upload ingestion, business domain validation, metadata 
 * persistence tracking, collection extraction, and lifecycle removal loops of critical claim files. 
 * Sitting seamlessly between the inbound API exposure controllers and the infrastructure data/storage layers, 
 * this contract remains entirely decoupled from physical file storage engine variations (such as local disk paths 
 * or cloud cloud object containers).
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface DocumentService {

    /**
     * Ingests, processes, and associates an incoming multi-part binary file attachment with an 
     * active insurance claim transaction resource.
     * <p>
     * Implementation constraints must enforce explicit payload validation routines (e.g., verifying 
     * allowed MIME formats like PDF, PNG, JPEG and total file size limits), securely stream the stream 
     * block via storage abstractions, map database metadata profiles, and transition the initial state 
     * engine status variables ahead of subsequent AI background orchestration workers.
     * </p>
     *
     * @param claimId the unique {@link UUID} operational tracking reference of the parent insurance claim resource
     * @param file the inbound {@link MultipartFile} data object carrying the raw binary chunk and client file data
     * @return an {@link UploadDocumentResponse} acknowledging successful storage extraction and ingestion metadata metrics
     */
    UploadDocumentResponse uploadDocument(UUID claimId, MultipartFile file);

    /**
     * Extracts a historical indexed log collection of standard metadata responses for all active, 
     * non-deleted supporting documents associated with a specific claim tracking reference.
     *
     * @param claimId the unique {@link UUID} operational tracking reference of the target insurance claim
     * @return a {@link List} of {@link DocumentResponse} payloads mapping all non-deleted files related to the claim; 
     * an empty list if no active documents exist
     */
    List<DocumentResponse> getDocumentsByClaimId(UUID claimId);

    /**
     * Retrieves structural meta information for a specific, single document file asset uniquely 
     * tracked by its database surrogate primary key identifier.
     *
     * @param documentId the unique {@code Long} database sequence identification key tracking the document resource
     * @return the corresponding client-facing {@link DocumentResponse} mapping the active metadata summary
     */
    DocumentResponse getDocumentById(Long documentId);

    /**
     * Coordinates the safe destruction and lifecycle elimination parameters of an uploaded document resource.
     * <p>
     * Implementations of this operation must trigger physical asset removal routines from the attached storage grid, 
     * clean up database record tracking fields or execute standard soft-deletion constraints safely to isolate the 
     * entity row from active workspace loops.
     * </p>
     *
     * @param documentId the unique {@code Long} database sequence identification key tracking the document resource to be purged
     */
    void deleteDocument(Long documentId);
}