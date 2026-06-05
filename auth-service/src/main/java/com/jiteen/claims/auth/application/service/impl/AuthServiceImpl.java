package com.jiteen.claims.auth.application.service.impl;

import com.jiteen.claims.auth.application.dto.request.LoginRequest;
import com.jiteen.claims.auth.application.dto.request.RegisterRequest;
import com.jiteen.claims.auth.application.dto.response.LoginResponse;
import com.jiteen.claims.auth.application.dto.response.RegisterResponse;
import com.jiteen.claims.auth.application.service.AuthService;
import com.jiteen.claims.auth.application.service.jwt.JwtService;
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

/**
 * Default implementation of {@link AuthService} for the claims authentication
 * domain.
 *
 * <p>This service orchestrates the registration and login workflows, applying
 * the relevant business rules and delegating to collaborating components for
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
     * Encoder used to securely hash raw passwords prior to persistence.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service responsible for issuing and validating JSON Web Tokens.
     */
    private final JwtService jwtService;

    /**
     * Registers a new user within the authentication domain.
     *
     * <p>The supplied email is normalized (trimmed and lowercased) and checked
     * for uniqueness among active (non-deleted) accounts. The raw password is
     * securely hashed before the user is persisted with the default role of
     * {@link Role#CUSTOMER} and an initial status of
     * {@link UserStatus#PENDING_VERIFICATION}.</p>
     *
     * @param request the registration request containing the user's credentials
     *                and profile details; must not be {@code null}
     * @return a {@link RegisterResponse} describing the newly created user
     * @throws IllegalArgumentException if an active user already exists with the
     *                                  supplied email
     */
    @Override
    public RegisterResponse register(final RegisterRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.debug("Processing registration request for email [{}]", normalizedEmail);

        if (userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
            log.warn("Registration failed: email [{}] is already in use", normalizedEmail);
            throw new IllegalArgumentException("A user with the provided email already exists");
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
     * <p>The supplied email is normalized (trimmed and lowercased) and used to
     * locate an active (non-deleted) user. The raw password is verified against
     * the stored password hash, and the account status is checked to ensure the
     * user is permitted to authenticate. Accounts in the
     * {@link UserStatus#ACTIVE} or {@link UserStatus#PENDING_VERIFICATION} states
     * may log in; accounts that are {@link UserStatus#INACTIVE} or
     * {@link UserStatus#LOCKED} are denied.</p>
     *
     * <p>On successful authentication, the user's last login timestamp is updated
     * and freshly signed access and refresh tokens are returned.</p>
     *
     * @param request the login request containing the user's email and password;
     *                must not be {@code null}
     * @return a {@link LoginResponse} containing the issued tokens and associated
     *         metadata
     * @throws IllegalArgumentException if the credentials are invalid or the
     *                                  account status does not permit login
     */
    @Override
    public LoginResponse login(final LoginRequest request) {
        final String normalizedEmail = normalizeEmail(request.getEmail());
        log.debug("Processing login request for email [{}]", normalizedEmail);

        final User user = userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: no active user found for email [{}]", normalizedEmail);
                    return new IllegalArgumentException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for email [{}]", normalizedEmail);
            throw new IllegalArgumentException("Invalid email or password");
        }

        switch (user.getStatus()) {
            case UserStatus.ACTIVE, UserStatus.PENDING_VERIFICATION -> log.debug(
                    "User [{}] passed status check with status [{}]", normalizedEmail, user.getStatus());
            case UserStatus.INACTIVE -> {
                log.warn("Login denied: account is inactive for email [{}]", normalizedEmail);
                throw new IllegalArgumentException("Account is inactive. Please contact support");
            }
            case UserStatus.LOCKED -> {
                log.warn("Login denied: account is locked for email [{}]", normalizedEmail);
                throw new IllegalArgumentException("Account is locked. Please contact support");
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
        final String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        log.info("Successfully authenticated user with id [{}]", user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
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