package com.jiteen.claims.auth.domain.repository;

import com.jiteen.claims.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for managing {@link User} entities within the
 * claims authentication domain.
 *
 * <p>This repository provides standard CRUD operations inherited from
 * {@link JpaRepository}, along with domain-specific query methods for looking up
 * users by email address. Variants that filter on {@code deletedAt IS NULL}
 * support soft-delete semantics by restricting results to active (non-deleted)
 * users.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user by their email address, regardless of soft-delete state.
     *
     * @param email the email address to search for; must not be {@code null}
     * @return an {@link Optional} containing the matching user, or
     *         {@link Optional#empty()} if none is found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Determines whether a user exists with the given email address, regardless
     * of soft-delete state.
     *
     * @param email the email address to check; must not be {@code null}
     * @return {@code true} if a matching user exists, otherwise {@code false}
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Retrieves an active (non-deleted) user by their email address.
     *
     * <p>Only users whose {@code deletedAt} field is {@code null} are
     * considered.</p>
     *
     * @param email the email address to search for; must not be {@code null}
     * @return an {@link Optional} containing the matching active user, or
     *         {@link Optional#empty()} if none is found
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Determines whether an active (non-deleted) user exists with the given
     * email address.
     *
     * <p>Only users whose {@code deletedAt} field is {@code null} are
     * considered.</p>
     *
     * @param email the email address to check; must not be {@code null}
     * @return {@code true} if a matching active user exists, otherwise
     *         {@code false}
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);
}