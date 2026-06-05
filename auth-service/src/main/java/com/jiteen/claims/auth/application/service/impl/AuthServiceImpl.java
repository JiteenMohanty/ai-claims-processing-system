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
     * Authenticates an existing user using the supplied credentials.
     *
     * <p>This operation is not yet implemented and will be provided in a future
     * iteration.</p>
     *
     * @param request the login request containing the user's email and password
     * @return never returns normally in the current implementation
     * @throws UnsupportedOperationException always, until login is implemented
     */
    @Override
    public LoginResponse login(final LoginRequest request) {
        throw new UnsupportedOperationException("Login functionality not implemented yet");
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