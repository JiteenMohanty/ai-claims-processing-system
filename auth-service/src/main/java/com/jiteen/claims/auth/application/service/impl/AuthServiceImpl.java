package com.jiteen.claims.auth.application.service.impl;

import com.jiteen.claims.auth.application.dto.request.LoginRequest;
import com.jiteen.claims.auth.application.dto.request.LogoutRequest;
import com.jiteen.claims.auth.application.dto.request.RefreshTokenRequest;
import com.jiteen.claims.auth.application.dto.request.RegisterRequest;
import com.jiteen.claims.auth.application.dto.response.LoginResponse;
import com.jiteen.claims.auth.application.dto.response.RefreshTokenResponse;
import com.jiteen.claims.auth.application.dto.response.RegisterResponse;
import com.jiteen.claims.auth.application.service.AuthService;
import com.jiteen.claims.auth.application.service.jwt.JwtService;
import com.jiteen.claims.auth.security.CustomUserDetailsService;
import com.jiteen.claims.auth.domain.entity.User;
import com.jiteen.claims.auth.domain.enums.Role;
import com.jiteen.claims.auth.domain.enums.UserStatus;
import com.jiteen.claims.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiteen.claims.auth.api.exception.UserAlreadyExistsException;
import com.jiteen.claims.auth.api.exception.InvalidCredentialsException;
import com.jiteen.claims.auth.api.exception.AccountLockedException;
import com.jiteen.claims.auth.api.exception.AccountInactiveException;
import com.jiteen.claims.auth.api.exception.InvalidRefreshTokenException;
import com.jiteen.claims.auth.api.exception.UserNotFoundException;
import com.jiteen.claims.auth.domain.entity.RefreshToken;
import com.jiteen.claims.auth.domain.repository.RefreshTokenRepository;

/**
 * Default implementation of {@link AuthService} for the claims authentication
 * domain.
 *
 * <p>
 * This service orchestrates the registration and login workflows, applying the
 * relevant business rules and delegating to collaborating components for
 * persistence, password hashing, and token issuance. All public operations are
 * executed within a transactional boundary.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Repository providing persistence operations for {@link User} entities.
     */
    private final UserRepository userRepository;

    /**
     * Repository providing persistence operations for refresh tokens.
     */
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Encoder used to securely hash raw passwords prior to persistence.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service responsible for issuing and validating JSON Web Tokens.
     */
    private final JwtService jwtService;

    /**
     * User details service used to evict stale cache entries on logout.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Registers a new user within the authentication domain.
     *
     * <p>
     * The supplied email is normalized (trimmed and lowercased) and checked for
     * uniqueness among active (non-deleted) accounts. The raw password is
     * securely hashed before the user is persisted with the default role of
     * {@link Role#CUSTOMER} and an initial status of
     * {@link UserStatus#PENDING_VERIFICATION}.</p>
     *
     * @param request the registration request containing the user's credentials
     * and profile details; must not be {@code null}
     * @return a {@link RegisterResponse} describing the newly created user
     * @throws IllegalArgumentException if an active user already exists with
     * the supplied email
     */
    @Override
    public RegisterResponse register(final RegisterRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.debug("Processing registration request for email [{}]", normalizedEmail);

        if (userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
            log.warn("Registration failed: email [{}] is already in use", normalizedEmail);
            throw new UserAlreadyExistsException("A user with the provided email already exists");
        }

        final User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        final User savedUser = userRepository.save(user);
        log.info("Successfully registered new user with id [{}]", savedUser.getId());

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .status(savedUser.getStatus())
                .build();
    }

    /**
     * Authenticates an existing user using the supplied credentials and issues
     * JWT access and refresh tokens upon success.
     *
     * <p>
     * The supplied email is normalized (trimmed and lowercased) and used to
     * locate an active (non-deleted) user. The raw password is verified against
     * the stored password hash, and the account status is checked to ensure the
     * user is permitted to authenticate. Accounts in the
     * {@link UserStatus#ACTIVE} or {@link UserStatus#PENDING_VERIFICATION}
     * states may log in; accounts that are {@link UserStatus#INACTIVE} or
     * {@link UserStatus#LOCKED} are denied.</p>
     *
     * <p>
     * On successful authentication, the user's last login timestamp is updated
     * and freshly signed access and refresh tokens are returned.</p>
     *
     * @param request the login request containing the user's email and
     * password; must not be {@code null}
     * @return a {@link LoginResponse} containing the issued tokens and
     * associated metadata
     * @throws IllegalArgumentException if the credentials are invalid or the
     * account status does not permit login
     */
    @Override
    public LoginResponse login(final LoginRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.debug("Processing login request for email [{}]", normalizedEmail);

        final User user = userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: no active user found for email [{}]", normalizedEmail);
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for email [{}]", normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        switch (user.getStatus()) {
            case UserStatus.ACTIVE, UserStatus.PENDING_VERIFICATION ->
                log.debug(
                        "User [{}] passed status check with status [{}]", normalizedEmail, user.getStatus());
            case UserStatus.INACTIVE -> {
                log.warn("Login denied: account is inactive for email [{}]", normalizedEmail);
                throw new AccountInactiveException("Account is inactive. Please contact support");
            }
            case UserStatus.LOCKED -> {
                log.warn("Login denied: account is locked for email [{}]", normalizedEmail);
                throw new AccountLockedException("Account is locked. Please contact support");
            }
            default -> {
                log.warn("Login denied: unsupported account status [{}] for email [{}]",
                        user.getStatus(), normalizedEmail);
                throw new IllegalArgumentException("Account status does not permit login");
            }
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        final String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name());

        final RefreshToken refreshTokenEntity = createRefreshToken(user);

        log.info("Successfully authenticated user with id [{}]", user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
    }

    /**
     * Refreshes an access token using a previously issued refresh token.
     *
     * <p>
     * The supplied refresh token is validated for signature integrity, issuer
     * correctness, and expiration. The subject is then extracted and used to
     * locate an active (non-deleted) user. The account status is verified to
     * ensure the user remains permitted to authenticate: accounts in the
     * {@link UserStatus#ACTIVE} or {@link UserStatus#PENDING_VERIFICATION}
     * states may refresh their token; accounts that are
     * {@link UserStatus#INACTIVE} or {@link UserStatus#LOCKED} are denied.</p>
     *
     * <p>
     * On success, a freshly signed access token is issued. This flow allows a
     * client to obtain a new access token without requiring the user to
     * re-authenticate with their email and password.</p>
     *
     * @param request the refresh token request containing the previously issued
     * refresh token; must not be {@code null}
     * @return a {@link RefreshTokenResponse} containing the newly issued access
     * token and associated metadata
     * @throws IllegalArgumentException if the refresh token is invalid, the
     * user cannot be found, or the account status does not permit token refresh
     */
    @Override
    public RefreshTokenResponse refreshToken(final RefreshTokenRequest request) {
        log.debug("Processing refresh token request");

        final String refreshToken = request.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Refresh failed: invalid refresh token");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        final RefreshToken storedToken
                = refreshTokenRepository
                        .findByTokenAndRevokedFalse(refreshToken)
                        .orElseThrow(() -> {
                            log.warn("Refresh failed: token not found or revoked");
                            return new InvalidRefreshTokenException(
                                    "Refresh token is invalid or revoked");
                        });

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Refresh failed: refresh token expired");

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired");
        }

        final String email = jwtService.extractUsername(refreshToken);
        log.debug("Refresh token resolved to subject [{}]", email);

        final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> {
                    log.warn("Refresh failed: no active user found for email [{}]", email);
                    return new UserNotFoundException("User not found");
                });

        switch (user.getStatus()) {
            case UserStatus.ACTIVE, UserStatus.PENDING_VERIFICATION ->
                log.debug(
                        "User [{}] passed status check with status [{}]", email, user.getStatus());
            case UserStatus.INACTIVE -> {
                log.warn("Refresh denied: account is inactive for email [{}]", email);
                throw new AccountInactiveException("Account is inactive. Please contact support");
            }
            case UserStatus.LOCKED -> {
                log.warn("Refresh denied: account is locked for email [{}]", email);
                throw new AccountLockedException("Account is locked. Please contact support");
            }
            default -> {
                log.warn("Refresh denied: unsupported account status [{}] for email [{}]",
                        user.getStatus(), email);
                throw new IllegalArgumentException("Account status does not permit login");
            }
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        final RefreshToken newRefreshToken
                = createRefreshToken(user);

        final String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name());

        log.info("Successfully refreshed access token for user with id [{}]", user.getId());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
    }

    /**
     * Revokes the refresh token supplied in the request, terminating the user's
     * authenticated session.
     *
     * <p>
     * The method resolves the raw token string from the {@link LogoutRequest},
     * loads the corresponding
     * {@link com.jiteen.claims.auth.domain.entity.RefreshToken} record from the
     * database, and marks it as revoked. Once revoked, the token is permanently
     * invalidated: any subsequent attempt to exchange it for a new access token
     * will be rejected by the refresh-token flow.</p>
     *
     * <p>
     * The operation fails fast with an
     * {@link com.jiteen.claims.auth.application.exception.InvalidRefreshTokenException}
     * under two distinct conditions:</p>
     * <ul>
     * <li>the token is not present in the {@code refresh_tokens} table — it was
     * never issued, or has already been purged by a scheduled cleanup job;
     * and</li>
     * <li>the token exists but its {@code revoked} flag is already {@code true}
     * — a duplicate logout or a replay of a previously processed request.</li>
     * </ul>
     *
     * <p>
     * On success, the revocation is persisted immediately and a structured log
     * entry is written at {@code INFO} level, capturing the owning user's
     * e-mail address to support audit trails without exposing the raw token
     * value.</p>
     *
     * @param request a {@link LogoutRequest} carrying the refresh token to
     * revoke; must not be {@code null}, and the enclosed token value must be
     * non-blank
     * @throws
     * com.jiteen.claims.auth.application.exception.InvalidRefreshTokenException
     * if the token is not found in the database, or if it has already been
     * revoked
     * @since 1.0
     */
    @Override
    public void logout(final LogoutRequest request) {

        log.debug("Processing logout request");

        final String refreshToken = request.getRefreshToken();

        final RefreshToken storedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (Boolean.TRUE.equals(storedToken.getRevoked())) {
            throw new InvalidRefreshTokenException("Refresh token already revoked");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        final String userEmail = storedToken.getUser().getEmail();
        customUserDetailsService.evictUserCache(userEmail);

        log.info("Successfully revoked refresh token for user [{}]", userEmail);
    }

    /**
     * Creates and persists a new refresh token for the supplied user.
     *
     * @param user the user receiving the refresh token
     * @return the persisted refresh token entity
     */
    private RefreshToken createRefreshToken(final User user) {

        final String token = jwtService.generateRefreshToken(user.getEmail());

        final RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Normalizes an email address by trimming surrounding whitespace and
     * converting it to lowercase.
     *
     * @param email the raw email address supplied by the caller
     * @return the normalized email address
     */
    private String normalizeEmail(final String email) {
        return email.trim().toLowerCase();
    }
}
