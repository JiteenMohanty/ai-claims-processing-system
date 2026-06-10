package com.jiteen.claims.auth.application.service;

import com.jiteen.claims.auth.application.dto.request.LoginRequest;
import com.jiteen.claims.auth.application.dto.request.LogoutRequest;
import com.jiteen.claims.auth.application.dto.request.RefreshTokenRequest;
import com.jiteen.claims.auth.application.dto.request.RegisterRequest;
import com.jiteen.claims.auth.application.dto.response.LoginResponse;
import com.jiteen.claims.auth.application.dto.response.RefreshTokenResponse;
import com.jiteen.claims.auth.application.dto.response.RegisterResponse;

/**
 * Application service defining the authentication operations available within
 * the claims authentication domain.
 *
 * <p>This contract abstracts the registration and login workflows, decoupling
 * the API layer from the underlying implementation details such as credential
 * hashing, persistence, and token issuance. Implementations are expected to
 * enforce all relevant business rules and security constraints.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public interface AuthService {

    /**
     * Registers a new user within the authentication domain.
     *
     * <p>Implementations are responsible for validating uniqueness of the
     * supplied email, securely hashing the raw password before persistence, and
     * establishing the initial account lifecycle status.</p>
     *
     * @param request the registration request containing the user's credentials
     *                and profile details; must not be {@code null}
     * @return a {@link RegisterResponse} describing the newly created user,
     *         excluding any sensitive credential data
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user using the supplied credentials.
     *
     * <p>Implementations are responsible for verifying the provided credentials
     * against the stored account and, upon success, issuing the access and
     * refresh tokens required for subsequent authenticated requests.</p>
     *
     * @param request the login request containing the user's email and password;
     *                must not be {@code null}
     * @return a {@link LoginResponse} containing the issued tokens and associated
     *         metadata
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes an access token using a previously issued refresh token.
     *
     * <p>Implementations are responsible for validating the supplied refresh
     * token and, upon success, issuing a new access token. The refresh token
     * must be valid and unexpired for the operation to succeed. This flow allows
     * a client to obtain a new access token without requiring the user to
     * re-authenticate with their email and password.</p>
     *
     * @param request the refresh token request containing the previously issued
     *                refresh token; must not be {@code null}
     * @return a {@link RefreshTokenResponse} containing the newly issued access
     *         token and associated metadata
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

     /**
     * Revokes the refresh token contained in the supplied request, effectively
     * terminating the user's authenticated session.
     *
     * <p>Once revoked, the refresh token can no longer be used to obtain new
     * access tokens. Any in-flight access tokens issued prior to this call
     * remain valid until their own expiry; downstream services are expected to
     * honour those tokens for the remainder of their TTL. This design is
     * intentional and consistent with stateless JWT architecture.</p>
     *
     * <p>This method is the primary entry point for logout and session
     * invalidation workflows. Implementations are responsible for persisting
     * the revocation state (e.g. marking the token as invalid in the
     * {@code refresh_tokens} table or a distributed deny-list) so that
     * subsequent refresh attempts are rejected.</p>
     *
     * @param request a {@link LogoutRequest} containing the refresh token to
     *                revoke; must not be {@code null}, and the enclosed token
     *                must be non-blank and previously issued by this service
     * @throws jakarta.validation.ConstraintViolationException if the request
     *         fails bean-validation (e.g. a blank refresh token)
     * @throws com.jiteen.claims.auth.application.exception.InvalidTokenException
     *         if the supplied refresh token is not recognised or has already
     *         been revoked
     * @since 1.0
     */
    void logout(LogoutRequest request);
}