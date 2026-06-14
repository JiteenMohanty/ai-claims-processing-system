package com.jiteen.claims.claim.api.exception;

import java.util.UUID;

/**
 * Custom unchecked domain exception thrown when a requested insurance claim resource 
 * cannot be located or extracted from the persistent system of record.
 * <p>
 * This exception extends {@link RuntimeException} to cleanly integrate with Spring's 
 * declarative transaction infrastructure, triggering an automatic rollback where appropriate. 
 * It aligns with the centralized fault-handling architecture established within the platform's 
 * reference microservices, providing specialized structural identification for resource-not-found 
 * edge conditions.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class ClaimNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ClaimNotFoundException} initializing the error context 
     * using the specific unique identifier of the missing claim resource.
     *
     * @param claimId the unique {@link UUID} tracking token mapping the target insurance claim record
     */
    public ClaimNotFoundException(UUID claimId) {
        super("Claim not found with id: " + claimId);
    }

    /**
     * Constructs a new {@code ClaimNotFoundException} populated with a customized, 
     * precise detailed message explaining the resolution failure scenario.
     *
     * @param message the qualitative plaintext string detailing the exception context
     */
    public ClaimNotFoundException(String message) {
        super(message);
    }
}