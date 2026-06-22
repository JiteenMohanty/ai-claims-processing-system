package com.jiteen.claims.claim.api.exception;

/**
 * Custom unchecked infrastructure exception thrown when an underlying data persistence
 * or storage operation encounters a failure within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This exception abstracts and encapsulates low-level transactional issues such as filesystem write
 * block failures, folder allocation errors, disk cleanup erasure faults, path traversal security constraints, 
 * or remote cloud storage protocol loops (such as future enterprise AWS S3 integrations). By extending 
 * {@link RuntimeException}, it integrates with the platform's centralized fault handling matrix and triggers 
 * standard automatic transaction rollbacks where appropriate.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code FileStorageException} populated with a clear, definitive message 
     * detailing the execution constraint or structural boundary anomaly.
     *
     * @param message the qualitative plaintext string detailing the exception context
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code FileStorageException} combining a detailed error narrative 
     * with the lower-level systemic cause to facilitate complete trace logging analyses.
     *
     * @param message the qualitative plaintext string detailing the exception context
     * @param cause the underlying root programmatic {@link Throwable} vector triggering the failure
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}