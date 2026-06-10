package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when an authentication or token refresh attempt is made using
 * an account that has been locked.
 *
 * <p>Locked accounts are not permitted to access protected resources until
 * administrative action is taken to restore them. This exception signals a
 * business-rule violation rather than a system failure: the request is
 * well-formed and the system is operating correctly, but the account's current
 * status prohibits the requested operation. It is typically translated into an
 * HTTP {@code 403 Forbidden} response at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class AccountLockedException extends AuthException {

    /**
     * Constructs a new {@code AccountLockedException} with the supplied detail
     * message.
     *
     * @param message the detail message describing the locked-account condition
     */
    public AccountLockedException(final String message) {
        super(message);
    }
}