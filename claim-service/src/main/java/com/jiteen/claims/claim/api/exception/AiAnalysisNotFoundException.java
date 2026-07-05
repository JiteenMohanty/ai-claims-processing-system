package com.jiteen.claims.claim.api.exception;

import java.util.UUID;

/**
 * Domain exception signaling that no AI analysis result exists for the
 * specified insurance claim identifier within the persistence layer.
 *
 * <p>
 * This exception is raised when a client queries the AI analysis result endpoint
 * for a claim whose asynchronous AI processing has not yet completed, or for a
 * claim that does not exist. The {@link GlobalExceptionHandler} maps this
 * exception to an HTTP 404 Not Found response.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public class AiAnalysisNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code AiAnalysisNotFoundException} for the specified claim.
     *
     * @param claimId the unique {@link UUID} of the claim for which no AI analysis
     *                result was found
     */
    public AiAnalysisNotFoundException(UUID claimId) {
        super("AI analysis result not found for claimId: " + claimId +
              ". Analysis may still be in progress.");
    }
}
