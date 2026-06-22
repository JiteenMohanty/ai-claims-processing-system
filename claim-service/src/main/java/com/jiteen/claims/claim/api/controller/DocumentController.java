package com.jiteen.claims.claim.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jiteen.claims.claim.application.dto.response.DocumentResponse;
import com.jiteen.claims.claim.application.dto.response.UploadDocumentResponse;
import com.jiteen.claims.claim.application.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller exposing public API ingress nodes for managing insurance claim 
 * supporting document attachments within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This boundary layer component manages multipart file upload processing, file retention lookups, 
 * and explicit removal workflows. It serves as the primary gateway for customers and audit officers 
 * to attach evidence (such as loss images, repair estimates, or medical bills) to claims, paving 
 * the path for subsequent asynchronous AI extraction pipelines.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Claim Documents", description = "REST APIs for supporting document upload, validation, and lifecycle metadata retrieval.")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Uploads and attaches a multi-part binary file resource to an active insurance claim record.
     * <p>
     * Consumes multipart/form-data payloads, triggers strict system validation rules concerning 
     * allowed MIME media extensions and maximum data volume parameters before persisting the asset 
     * to the storage layer and creating localized transaction tracking indices.
     * </p>
     *
     * @param claimId the unique {@link UUID} master tracking tracking token mapping the parent insurance claim
     * @param file the inbound raw binary {@link MultipartFile} wrapper carrying the payload block
     * @return a {@link ResponseEntity} wrapping the structural {@link UploadDocumentResponse} metadata payload with HTTP 201 Created status
     */
    @PostMapping(value = "/claims/{claimId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload claim document", description = "Accepts a multipart file attachment (PDF/JPEG/PNG, max 10MB), persists it to storage, and registers it to the claim.")
    public ResponseEntity<UploadDocumentResponse> uploadDocument(
            @PathVariable UUID claimId,
            @RequestParam("file") MultipartFile file) {
        UploadDocumentResponse response = documentService.uploadDocument(claimId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Extracts a historical compilation index log containing all non-deleted supporting document records 
     * registered under a target insurance claim resource handle.
     *
     * @param claimId the unique {@link UUID} master tracking tracking token mapping the target parent claim
     * @return a {@link ResponseEntity} wrapping a {@link List} of active {@link DocumentResponse} items with HTTP 200 OK status
     */
    @GetMapping("/claims/{claimId}/documents")
    @Operation(summary = "Get all documents for a claim", description = "Retrieves an indexed array listing of metadata profiles for all active supporting documents linked to the specified claim.")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByClaimId(
            @PathVariable UUID claimId) {
        List<DocumentResponse> response = documentService.getDocumentsByClaimId(claimId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches detailed structural meta definitions matching a single specialized document reference key.
     *
     * @param documentId the long-integer surrogate sequence index reference tracking the target document record
     * @return a {@link ResponseEntity} wrapping the standard operational {@link DocumentResponse} block with HTTP 200 OK status
     */
    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Get document metadata by ID", description = "Fetches separate localized transaction tracking indices for a specific document asset reference block.")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @PathVariable Long documentId) {
        DocumentResponse response = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Decommissions an active supporting document attachment resource from system lookups and deletes its backing data files.
     * <p>
     * Triggers a deep lifecycle validation path that cleans up physical bytes allocated inside the storage grid 
     * before modifying persistent records using soft-delete mechanics to completely isolate the entity from incoming query streams.
     * </p>
     *
     * @param documentId the long-integer surrogate sequence index reference tracking the target document record to be erased
     * @return a stateless {@link ResponseEntity} container mapping an HTTP 204 No Content operational success marker
     */
    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Delete document", description = "Permanently cleans up binary disk layout allocations for the target document asset and handles metadata row soft-deletion rules.")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}