package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when an authentication or token refresh attempt is made using
 * an account that is inactive.
 *
 * <p>Inactive accounts are not permitted to authenticate or access protected
 * resources until they are reactivated. This exception signals a business-rule
 * violation rather than a system failure: the request is well-formed and the
 * system is operating correctly, but the account's current status prohibits the
 * requested operation. It is typically translated into an HTTP
 * {@code 403 Forbidden} response at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class AccountInactiveException extends AuthException {

    /**
     * Constructs a new {@code AccountInactiveException} with the supplied detail
     * message.
     *
     * @param message the detail message describing the inactive-account condition
     */
    public AccountInactiveException(final String message) {
        super(message);
    }
}