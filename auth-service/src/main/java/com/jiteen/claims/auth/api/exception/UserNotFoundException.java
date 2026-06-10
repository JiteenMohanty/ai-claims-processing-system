package com.jiteen.claims.auth.api.exception;

/**
 * Exception thrown when a user cannot be located for a given identifier, such as
 * an email address or user ID.
 *
 * <p>This exception typically indicates that the requested user does not exist
 * or is no longer available for the requested operation (for example, the
 * account has been removed or soft-deleted). It signals a business-domain error
 * rather than a system failure: the request is well-formed and the system is
 * operating correctly, but no matching user could be resolved. Depending on the
 * context, it is commonly translated into an HTTP {@code 404 Not Found} response
 * at the API boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public class UserNotFoundException extends AuthException {

    /**
     * Constructs a new {@code UserNotFoundException} with the supplied detail
     * message.
     *
     * @param message the detail message identifying the user that could not be
     *                located
     */
    public UserNotFoundException(final String message) {
        super(message);
    }
}