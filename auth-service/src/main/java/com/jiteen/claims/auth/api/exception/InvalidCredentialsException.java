package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when user authentication fails due to invalid credentials,
 * such as an incorrect email address or password.
 *
 * <p>For security reasons, this exception intentionally avoids revealing which
 * specific credential was incorrect. Disclosing whether the email or the
 * password was at fault could aid account enumeration and brute-force attacks;
 * therefore, a single, generic failure message should be surfaced to the
 * caller. It is typically translated into an HTTP {@code 401 Unauthorized}
 * response at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class InvalidCredentialsException extends AuthException {

    /**
     * Constructs a new {@code InvalidCredentialsException} with the supplied
     * detail message.
     *
     * @param message the detail message describing the authentication failure,
     *                which should not disclose which credential was incorrect
     */
    public InvalidCredentialsException(final String message) {
        super(message);
    }
}