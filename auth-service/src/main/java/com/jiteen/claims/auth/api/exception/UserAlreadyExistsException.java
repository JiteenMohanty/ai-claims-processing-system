package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when a registration attempt is made using an email address
 * that already belongs to an existing user account.
 *
 * <p>This exception signals a business-rule violation rather than a system
 * failure: the request is well-formed and the system is operating correctly,
 * but the requested operation cannot proceed because email uniqueness must be
 * preserved across active accounts. It is typically translated into an HTTP
 * {@code 409 Conflict} response at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class UserAlreadyExistsException extends AuthException {

    /**
     * Constructs a new {@code UserAlreadyExistsException} with the supplied
     * detail message.
     *
     * @param message the detail message describing the conflicting registration
     *                attempt
     */
    public UserAlreadyExistsException(final String message) {
        super(message);
    }
}