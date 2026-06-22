package com.jiteen.claims.claim.api.exception;

/**
 * Custom unchecked domain exception thrown when a user attempts to upload a supporting
 * claim document whose Multipurpose Internet Mail Extensions (MIME) content type is not 
 * officially supported by the insurance platform tracking infrastructure.
 * <p>
 * This exception extends {@link RuntimeException} to automatically participate in Spring's 
 * declarative transaction rollback mechanisms. It safely segregates media type mismatch errors 
 * from general multipart ingestion bugs, allowing the centralized exception handling layer 
 * to surface precise bad-request validation details to the client layer.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class InvalidFileTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidFileTypeException} initializing the structural error 
     * message using the explicit unsupported content type string detected at application ingress.
     *
     * @param contentType the unsupported MIME type string extracted from the file payload
     */
    public InvalidFileTypeException(String contentType) {
        super("Unsupported file type: " + contentType);
    }
}