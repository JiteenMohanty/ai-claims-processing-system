package com.jiteen.claims.auth.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data Transfer Object representing a standardized error response returned by
 * the claims authentication domain.
 *
 * <p>This DTO provides a consistent structure for conveying error details to API
 * consumers, including the time of occurrence, the HTTP status, a short error
 * descriptor, a human-readable message, and the request path that triggered the
 * error. It is typically populated by a centralized exception handler to ensure
 * uniform error reporting across the API.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse implements Serializable {

    /**
     * The instant at which the error occurred.
     *
     * <p>Captured in UTC to provide an unambiguous, timezone-independent
     * reference for when the error was generated.</p>
     */
    private Instant timestamp;

    /**
     * The HTTP status code associated with the error.
     *
     * <p>Mirrors the numeric status returned in the HTTP response (for example,
     * {@code 400} or {@code 401}).</p>
     */
    private Integer status;

    /**
     * A short, machine-friendly descriptor of the error.
     *
     * <p>Typically corresponds to the HTTP status reason phrase (for example,
     * {@code "Bad Request"} or {@code "Unauthorized"}).</p>
     */
    private String error;

    /**
     * A human-readable message describing the error.
     *
     * <p>Provides additional context about the cause of the failure to aid
     * consumers in diagnosing the issue.</p>
     */
    private String message;

    /**
     * The request path that triggered the error.
     *
     * <p>Identifies the endpoint associated with the failed request (for example,
     * {@code "/api/v1/auth/login"}).</p>
     */
    private String path;
}