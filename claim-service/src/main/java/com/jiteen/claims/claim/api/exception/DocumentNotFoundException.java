package com.jiteen.claims.claim.api.exception;

/**
 * Custom unchecked domain exception thrown when a requested claim supporting document 
 * resource cannot be located within the persistent system of record or has been soft-deleted.
 * <p>
 * This exception extends {@link RuntimeException} to cleanly integrate with Spring's 
 * declarative transaction infrastructure, triggering an automatic rollback where appropriate. 
 * It mirrors the exact architectural design conventions established by {@code ClaimNotFoundException}, 
 * providing specialized structural identification for document-specific lookup failures within the 
 * platform's global exception handling lifecycle tier.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class DocumentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code DocumentNotFoundException} initializing the error context 
     * using the specific unique sequence identifier of the missing document resource.
     *
     * @param documentId the unique surrogate database primary key tracking the target document record
     */
    public DocumentNotFoundException(Long documentId) {
        super("Document not found with id: " + documentId);
    }
}