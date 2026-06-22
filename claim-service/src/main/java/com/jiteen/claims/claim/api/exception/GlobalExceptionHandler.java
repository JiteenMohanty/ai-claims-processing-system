package com.jiteen.claims.claim.api.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized interceptor and global fault-handling component for the Claim
 * microservice presentation layer.
 * <p>
 * This class leverages Spring's {@link RestControllerAdvice} mechanism to
 * capture, catalog, and transform downstream exceptions into unified, secure,
 * and production-grade REST API error response payloads. It decouples exception
 * propagation states from client exposures, handles structural data
 * transformation logging, and actively sanitizes unexpected framework stacks to
 * protect internal architectural definitions.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches and processes instances of {@link ClaimNotFoundException} when a
     * targeted claim resource is absent.
     * <p>
     * Emits a warnings-level diagnostic tracking entry and translates the
     * domain exception context cleanly into a standard 404 HTTP Not Found
     * structured map payload.
     * </p>
     *
     * @param ex the caught {@link ClaimNotFoundException} domain instance
     * @param request the active inbound {@link HttpServletRequest} tracking
     * vector
     * @return a {@link ResponseEntity} wrapping the standardized error response
     * payload schema with a 404 status
     */
    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClaimNotFoundException(ClaimNotFoundException ex, HttpServletRequest request) {
        log.warn("Claim resource look-up failure encountered: {} at path: {}", ex.getMessage(), request.getRequestURI());
        Map<String, Object> errorPayload = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorPayload);
    }

    /**
     * Intercepts and parses JSR-380 / Jakarta Bean Validation constraint
     * contract failures generated during inbound controller body argument
     * validation.
     * <p>
     * Compiles individual field validation messages into a single localized
     * string token collection while logging the resulting anomalies at the
     * warning severity tier.
     * </p>
     *
     * @param ex the targeted framework {@link MethodArgumentNotValidException}
     * object representation
     * @param request the active inbound {@link HttpServletRequest} tracking
     * vector
     * @return a {@link ResponseEntity} wrapping the standardized error response
     * payload schema with a 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String constraintViolations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Inbound payload integrity constraints violated: [{}] at path: {}", constraintViolations, request.getRequestURI());
        Map<String, Object> errorPayload = buildErrorResponse(HttpStatus.BAD_REQUEST, constraintViolations, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload);
    }

    /**
     * Processes runtime instances of {@link IllegalArgumentException} triggered
     * by localized programmatic validation or parameter validation failures.
     *
     * @param ex the underlying runtime {@link IllegalArgumentException} target
     * context
     * @param request the active inbound {@link HttpServletRequest} tracking
     * vector
     * @return a {@link ResponseEntity} wrapping the standardized error response
     * payload schema with a 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument lifecycle conflict detected: {} at path: {}", ex.getMessage(), request.getRequestURI());
        Map<String, Object> errorPayload = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload);
    }

    /**
     * Fallback execution node managing all generic, unhandled system runtime
     * exceptions and un-captured faults.
     * <p>
     * Securely generates a generalized message context to protect backend
     * ecosystem infrastructure details from leaking, while logging full stack
     * trace components at the error level for engineering remediation analysis.
     * </p>
     *
     * @param ex the root unhandled generic {@link Exception} context
     * @param request the active inbound {@link HttpServletRequest} tracking
     * vector
     * @return a {@link ResponseEntity} wrapping the standardized error response
     * payload schema with a 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled critical core-system exception captured at path: " + request.getRequestURI(), ex);
        Map<String, Object> errorPayload = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorPayload);
    }

    /**
     * Utility orchestrator building the standardized enterprise map structure
     * used across platform microservices.
     *
     * @param status the concrete target operational {@link HttpStatus} code
     * definition
     * @param message the contextual error narrative or structural breakdown
     * mapping details
     * @param request the active inbound {@link HttpServletRequest} reference
     * providing the request path URI
     * @return a insertion-ordered {@link Map} populated with core diagnostic
     * keys matching the enterprise REST specifications
     */
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", request.getRequestURI());
        return errorDetails;
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentNotFoundException(
            DocumentNotFoundException ex,
            HttpServletRequest request) {

        log.warn(
                "Document resource look-up failure encountered: {} at path: {}",
                ex.getMessage(),
                request.getRequestURI());

        Map<String, Object> errorPayload
                = buildErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorPayload);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileTypeException(
            InvalidFileTypeException ex,
            HttpServletRequest request) {

        log.warn(
                "Unsupported document media type detected: {} at path: {}",
                ex.getMessage(),
                request.getRequestURI());

        Map<String, Object> errorPayload
                = buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorPayload);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceededException(
            FileSizeExceededException ex,
            HttpServletRequest request) {

        log.warn(
                "Document upload exceeds configured storage thresholds: {} at path: {}",
                ex.getMessage(),
                request.getRequestURI());

        Map<String, Object> errorPayload
                = buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorPayload);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request) {

        log.error(
                "Document storage subsystem failure detected at path: {}",
                request.getRequestURI(),
                ex);

        Map<String, Object> errorPayload
                = buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        request);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorPayload);
    }

}
