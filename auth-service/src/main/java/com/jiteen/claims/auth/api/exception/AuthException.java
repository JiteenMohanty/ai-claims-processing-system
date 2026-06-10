package com.jiteen.claims.auth.api.exception;

/**
 * Abstract base type for all authentication-related exceptions within the
 * claims authentication domain.
 *
 * <p>This class serves as the common ancestor for domain-specific
 * authentication failures, enabling consistent handling and translation of such
 * errors at the API boundary. As an unchecked exception, it does not impose
 * handling obligations on callers, allowing failures to propagate to a
 * centralized exception handler.</p>
 *
 * <p>Concrete subclasses should represent specific failure scenarios (for
 * example, invalid credentials or denied account access) and supply an
 * appropriate, human-readable message.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public abstract class AuthException extends RuntimeException {

    /**
     * Constructs a new {@code AuthException} with the supplied detail message.
     *
     * @param message the detail message describing the authentication failure
     */
    protected AuthException(final String message) {
        super(message);
    }
}