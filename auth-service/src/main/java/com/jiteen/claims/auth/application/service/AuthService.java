package com.jiteen.claims.auth.application.service;

import com.jiteen.claims.auth.application.dto.request.RegisterRequest;
import com.jiteen.claims.auth.application.dto.response.RegisterResponse;
import com.jiteen.claims.auth.domain.entity.User;
import com.jiteen.claims.auth.domain.enums.Role;
import com.jiteen.claims.auth.domain.enums.UserStatus;
import com.jiteen.claims.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service responsible for orchestrating authentication-related
 * operations within the claims authentication domain.
 *
 * <p>
 * This service encapsulates the business logic for user registration,
 * enforcing domain invariants such as email uniqueness and applying sensible
 * defaults for newly created accounts.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Password hashing is not yet implemented. The raw
 * password is temporarily stored in the {@code passwordHash} field and will be
 * replaced with a BCrypt-hashed value in a subsequent iteration.
 * </p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Repository providing persistence operations for {@link User} entities.
     */
    private final UserRepository userRepository;

    /**
     * Registers a new user within the claims authentication domain.
     *
     * <p>
     * The registration process enforces the following business rules:
     * </p>
     * <ol>
     * <li>The provided email address must be unique among active users.</li>
     * <li>If a user with the given email already exists, an
     * {@link IllegalArgumentException} is thrown.</li>
     * <li>A new {@link User} entity is created with the role
     * {@link Role#CUSTOMER} and status
     * {@link UserStatus#PENDING_VERIFICATION}.</li>
     * <li>The entity is persisted via the {@link UserRepository}.</li>
     * </ol>
     *
     * <p>
     * <strong>Note:</strong> The raw password is temporarily stored in the
     * {@code passwordHash} field; secure hashing will be introduced in a later
     * step.
     * </p>
     *
     * @param request the registration request containing the user's details;
     *                must not be {@code null}
     * @return a {@link RegisterResponse} describing the newly created user
     * @throws IllegalArgumentException if a user with the given email already
     *                                  exists
     */
    @Transactional
    public RegisterResponse register(final RegisterRequest request) {

        final String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "A user with the provided email already exists");
        }

        final User user = User.builder()
                .email(normalizedEmail)
                // TODO: Replace with BCrypt-hashed password in the next step.
                .passwordHash(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.CUSTOMER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        final User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .status(savedUser.getStatus())
                .build();
    }
}