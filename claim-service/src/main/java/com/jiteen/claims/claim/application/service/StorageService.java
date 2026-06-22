package com.jiteen.claims.claim.application.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage Abstraction Layer interface defining the baseline infrastructure contract 
 * for managing physical binary document assets within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This interface decouples the core business logic and orchestration workflows of the application layer 
 * from physical persistence mechanics. It isolates higher tiers from structural file handling constraints, 
 * ensuring an entirely implementation-agnostic architecture. 
 * </p>
 * <p>
 * Architected for seamless scalability, this specification facilitates fluid migration across variable storage 
 * providers. While Phase 4 targets an initial standalone local filesystem architecture, subsequent enterprise 
 * deployment paths can transition directly to cloud native object storage architectures (such as AWS S3 or Azure Blob Storage) 
 * without requiring any modifications or breaking structural changes to consuming business service layers.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface StorageService {

    /**
     * Ingests, sanitizes, and streams an incoming binary multi-part request stream file asset into the targeted persistent store.
     * <p>
     * Implementations of this method are expected to execute cryptographic security checks, sanitize names to prevent path traversal 
     * vulnerabilities, distribute storage boundaries safely, and allocate a unique structural resource pointer signature.
     * </p>
     *
     * @param file the inbound {@link MultipartFile} wrapper carrying the binary payload and metadata payload context
     * @return a unique plaintext path identifier token or cloud resource URI pointing exactly to the stored location of the binary asset
     */
    String store(MultipartFile file);

    /**
     * Purges a distinct physical binary document asset from the persistent storage pool using its unique location reference string.
     * <p>
     * This operation completely de-allocates storage allocations mapped to the token. Implementations must handle safe cleanups 
     * and log resource states appropriately without exposing infrastructure exceptions.
     * </p>
     *
     * @param storagePath the unique structural resource path locator token or cloud URI tracking the target asset to be deleted
     */
    void delete(String storagePath);

    /**
     * Asserts the absolute state presence and accessibility of a specific binary asset tracking reference within the underlying storage grid.
     *
     * @param storagePath the unique structural resource path locator token or cloud URI targeting the query verification validation check
     * @return {@code true} if the binary file exists and remains programmatically retrievable; {@code false} otherwise
     */
    boolean exists(String storagePath);
}