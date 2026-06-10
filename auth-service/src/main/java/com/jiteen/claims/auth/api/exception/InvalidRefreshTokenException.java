package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when a refresh token is invalid, expired, malformed, revoked,
 * or otherwise fails validation.
 *
 * <p>When a valid refresh token cannot be supplied, the token refresh flow
 * cannot proceed and the client must re-authenticate using their credentials to
 * obtain a new set of tokens. This exception represents an authentication-related
 * business failure rather than a system failure: the request is well-formed and
 * the system is operating correctly, but the supplied token does not satisfy the
 * validation requirements. It is typically translated into an HTTP
 * {@code 401 Unauthorized} response at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class InvalidRefreshTokenException extends AuthException {

    /**
     * Constructs a new {@code InvalidRefreshTokenException} with the supplied
     * detail message.
     *
     * @param message the detail message describing why the refresh token failed
     *                validation
     */
    public InvalidRefreshTokenException(final String message) {
        super(message);
    }
}