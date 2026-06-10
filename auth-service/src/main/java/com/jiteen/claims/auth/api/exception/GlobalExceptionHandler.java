package com.jiteen.claims.auth.api.exception;

import com.jiteen.claims.auth.application.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

import com.jiteen.claims.auth.api.exception.UserAlreadyExistsException;
import com.jiteen.claims.auth.api.exception.InvalidCredentialsException;
import com.jiteen.claims.auth.api.exception.AccountLockedException;
import com.jiteen.claims.auth.api.exception.AccountInactiveException;
import com.jiteen.claims.auth.api.exception.InvalidRefreshTokenException;
import com.jiteen.claims.auth.api.exception.UserNotFoundException;

/**
 * Centralized exception handler for the claims authentication domain.
 *
 * <p>This advice intercepts exceptions raised by the API layer and translates
 * them into consistent, well-structured {@link ErrorResponse} payloads. By
 * standardizing error handling in a single location, it ensures uniform status
 * codes, messages, and logging behavior across all endpoints while avoiding the
 * exposure of sensitive internal details such as stack traces.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link IllegalArgumentException} instances raised by the
     * application, typically as a result of invalid input or violated business
     * rules.
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {

        log.warn("Illegal argument on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} instances raised when
     * request payload validation fails.
     *
     * <p>All field-level validation messages are collected and joined into a
     * single comma-separated string to provide a concise summary of the
     * validation failures.</p>
     *
     * @param ex      the thrown validation exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {

        final String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed on request [{}]: {}", request.getRequestURI(), message);

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all otherwise-unhandled {@link Exception} instances, serving as a
     * safety net for unexpected errors.
     *
     * <p>To avoid leaking sensitive implementation details, stack traces are not
     * exposed in the response; a generic message is returned instead while the
     * full exception is logged for diagnostics.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            final Exception ex,
            final HttpServletRequest request) {

        log.error("Unexpected error on request [{}]", request.getRequestURI(), ex);

        final ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Constructs a standardized {@link ErrorResponse} from the supplied status,
     * message, and request context.
     *
     * @param status  the HTTP status to report
     * @param message the human-readable error message
     * @param request the current HTTP request, used to capture the request path
     * @return a populated {@link ErrorResponse}
     */
    private ErrorResponse buildErrorResponse(
            final HttpStatus status,
            final String message,
            final HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

        /**
     * Handles {@link UserAlreadyExistsException} instances raised when a
     * registration attempt uses an email address that already belongs to an
     * existing user account.
     *
     * <p>This represents a business-rule violation rather than a system failure
     * and is mapped to an HTTP 409 (Conflict) response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 409 (Conflict)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            final UserAlreadyExistsException ex,
            final HttpServletRequest request) {

        log.warn("User already exists on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

        /**
     * Handles {@link InvalidCredentialsException} instances raised when user
     * authentication fails due to invalid credentials, such as an incorrect
     * email address or password.
     *
     * <p>For security reasons, the response does not disclose which specific
     * credential was incorrect. This represents an authentication failure and is
     * mapped to an HTTP 401 (Unauthorized) response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 401 (Unauthorized)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            final InvalidCredentialsException ex,
            final HttpServletRequest request) {

        log.warn("Invalid credentials on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

        /**
     * Handles {@link AccountLockedException} instances raised when an
     * authentication or token refresh attempt is made using an account that has
     * been locked.
     *
     * <p>Locked accounts are not permitted to access protected resources until
     * administrative action is taken. This represents a business-rule violation
     * rather than a system failure and is mapped to an HTTP 403 (Forbidden)
     * response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 403 (Forbidden)
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            final AccountLockedException ex,
            final HttpServletRequest request) {

        log.warn("Account locked on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

        /**
     * Handles {@link AccountInactiveException} instances raised when an
     * authentication or token refresh attempt is made using an account that is
     * inactive.
     *
     * <p>Inactive accounts are not permitted to authenticate or access protected
     * resources until reactivated. This represents a business-rule violation
     * rather than a system failure and is mapped to an HTTP 403 (Forbidden)
     * response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 403 (Forbidden)
     */
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountInactiveException(
            final AccountInactiveException ex,
            final HttpServletRequest request) {

        log.warn("Account inactive on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

        /**
     * Handles {@link InvalidRefreshTokenException} instances raised when a
     * refresh token is invalid, expired, malformed, revoked, or otherwise fails
     * validation.
     *
     * <p>When a valid refresh token cannot be supplied, the client must
     * re-authenticate. This represents an authentication-related business failure
     * rather than a system failure and is mapped to an HTTP 401 (Unauthorized)
     * response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 401 (Unauthorized)
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
            final InvalidRefreshTokenException ex,
            final HttpServletRequest request) {

        log.warn("Invalid refresh token on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

        /**
     * Handles {@link UserNotFoundException} instances raised when a user cannot
     * be located for a given identifier, such as an email address or user ID.
     *
     * <p>This typically indicates that the requested user does not exist or is no
     * longer available for the requested operation. It represents a
     * business-domain error rather than a system failure and is mapped to an HTTP
     * 404 (Not Found) response.</p>
     *
     * @param ex      the thrown exception
     * @param request the current HTTP request
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with
     *         HTTP 404 (Not Found)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            final UserNotFoundException ex,
            final HttpServletRequest request) {

        log.warn("User not found on request [{}]: {}", request.getRequestURI(), ex.getMessage());

        final ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}