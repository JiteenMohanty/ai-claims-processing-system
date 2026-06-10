package com.jiteen.claims.auth.api.controller;

import com.jiteen.claims.auth.application.dto.request.RegisterRequest;
import com.jiteen.claims.auth.application.dto.response.RegisterResponse;
import com.jiteen.claims.auth.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.jiteen.claims.auth.application.dto.request.LoginRequest;
import com.jiteen.claims.auth.application.dto.request.LogoutRequest;
import com.jiteen.claims.auth.application.dto.request.RefreshTokenRequest;
import com.jiteen.claims.auth.application.dto.response.LoginResponse;
import com.jiteen.claims.auth.application.dto.response.RefreshTokenResponse;

/**
 * REST controller exposing authentication-related endpoints for the claims
 * authentication domain.
 *
 * <p>
 * This controller serves as the API boundary for user-facing authentication
 * operations, delegating business logic to the {@link AuthService}. All request
 * payloads are validated using Jakarta Validation prior to processing.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    /**
     * Application service that encapsulates authentication business logic.
     */
    private final AuthService authService;

    /**
     * Registers a new user within the claims authentication domain.
     *
     * <p>
     * Accepts a validated {@link RegisterRequest}, delegates creation of the
     * user to the {@link AuthService}, and returns the resulting
     * {@link RegisterResponse} with an HTTP {@code 201 Created} status.</p>
     *
     * @param request the registration request payload; validated and must not
     * be {@code null}
     * @return a {@link ResponseEntity} containing the {@link RegisterResponse}
     * and an HTTP {@code 201 Created} status
     */
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the CUSTOMER role and a "
            + "PENDING_VERIFICATION status."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "User successfully registered",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = RegisterResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload or validation failure",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "409",
                description = "A user with the provided email already exists",
                content = @Content
        )
    })
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody final RegisterRequest request) {
        final RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and issues JWT access and refresh tokens.
     *
     * <p>
     * This endpoint validates the supplied credentials and, upon successful
     * authentication, returns a {@link LoginResponse} containing the issued
     * tokens and associated metadata. The request payload is validated at the
     * API boundary prior to delegation.</p>
     *
     * @param request the login request containing the user's email and
     * password; must be valid and non-{@code null}
     * @return a {@link ResponseEntity} with HTTP 200 and the issued tokens on
     * successful authentication
     */
    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates a user using email and password and returns JWT access and refresh tokens."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Successful authentication",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = LoginResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request payload",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Account locked or inactive",
                content = @Content
        )
    })
    @PostMapping(
            path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody final LoginRequest request) {
        final LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes a user's access token using a valid refresh token.
     *
     * <p>
     * This endpoint validates the supplied refresh token and, upon success,
     * returns a {@link RefreshTokenResponse} containing a newly issued access
     * token and associated metadata. The user is not required to
     * re-authenticate with their email and password. The request payload is
     * validated at the API boundary prior to delegation.</p>
     *
     * @param request the refresh token request containing the previously issued
     * refresh token; must be valid and non-{@code null}
     * @return a {@link ResponseEntity} with HTTP 200 and the newly issued
     * access token on successful refresh
     */
    @Operation(
            summary = "Refresh an access token",
            description = "Validates a refresh token and issues a new JWT access token without requiring re-authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Access token successfully refreshed",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = RefreshTokenResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid refresh token or validation failure",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content
        )
    })
    @PostMapping(
            path = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody final RefreshTokenRequest request) {
        final RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Revokes the supplied refresh token and invalidates the user's session.
     *
     * <p>This endpoint is intentionally public — it does not require a JWT
     * access token in the {@code Authorization} header. Logout is performed
     * exclusively via the refresh token carried in the request body, which
     * ensures that clients can always log out even after their short-lived
     * access token has expired.</p>
     *
     * <p>On success the endpoint returns HTTP {@code 204 No Content}. The
     * caller should discard both the refresh token and any access tokens held
     * locally, as the refresh token can no longer be used to obtain new access
     * tokens.</p>
     *
     * @param request a {@link LogoutRequest} containing the refresh token to
     *                revoke; validated by Jakarta Validation before the
     *                method body is entered
     * @return a {@link ResponseEntity} with HTTP status {@code 204 No Content}
     *         and an empty body on successful revocation
     * @since 1.0
     */
    @Operation(
            summary = "Logout user",
            description = "Revokes a refresh token and invalidates the user's session."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful — refresh token has been revoked"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token — token not found or already revoked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — request could not be authenticated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @PostMapping(
            path = "/logout",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> logout(
            @Valid @RequestBody final LogoutRequest request) {
 
        authService.logout(request);
 
        return ResponseEntity.noContent().build();
    }
}
