package com.jiteen.claims.claim.api.exception;

/**
 * Custom unchecked exception thrown when an uploaded insurance claim document 
 * exceeds the platform's maximum permitted file size constraint configuration.
 * <p>
 * This exception extends {@link RuntimeException} to cleanly integrate with Spring's 
 * declarative transaction infrastructure, triggering an automatic rollback where appropriate. 
 * It is utilized during multi-part stream evaluation inside the document service tier to 
 * prevent system infrastructure exploitation, storage boundary exhaustion, and denial-of-service conditions.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class FileSizeExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code FileSizeExceededException} compiling a structured, 
     * precise detailed error message using the active and maximum allowed payload byte metrics.
     *
     * @param actualSize the absolute byte size of the uploaded multi-part binary file asset
     * @param maxAllowedSize the maximum physical size parameter configured and permitted by the platform in bytes
     */
    public FileSizeExceededException(long actualSize, long maxAllowedSize) {
        super("File size exceeds maximum allowed size. Actual: " + actualSize + " bytes, Allowed: " + maxAllowedSize + " bytes");
    }
}