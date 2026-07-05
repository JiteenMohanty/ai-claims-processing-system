package com.jiteen.claims.auth.security;

import com.jiteen.claims.auth.config.CacheNames;
import com.jiteen.claims.auth.domain.entity.User;
import com.jiteen.claims.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security {@link UserDetailsService} implementation that loads user
 * accounts from the application database.
 *
 * <p>This service replaces Spring Boot's default in-memory
 * {@code UserDetailsService} bean and integrates the authentication layer with
 * the application's persistence tier. It is discovered automatically by Spring
 * Security's {@code DaoAuthenticationProvider} via the {@link Service}
 * stereotype and used during every username-and-password authentication
 * attempt.</p>
 *
 * <p>The lookup strategy enforces two invariants that are critical for a
 * production system:</p>
 * <ul>
 *   <li><strong>Soft-delete awareness</strong> — only users whose
 *       {@code deletedAt} field is {@code null} are considered active. Deleted
 *       accounts are invisible to the authentication layer and will result in a
 *       {@link UsernameNotFoundException}, preventing reuse of a deleted
 *       account's credentials.</li>
 *   <li><strong>Email normalisation</strong> — the supplied username is
 *       trimmed and lowercased before the database query is issued, making
 *       authentication case-insensitive and resilient to leading/trailing
 *       whitespace introduced by client-side form handling.</li>
 * </ul>
 *
 * <p>The {@link User} entity is never exposed directly to Spring Security.
 * Instead it is adapted to a {@link UserPrincipal} via
 * {@link UserPrincipal#fromUser(User)}, keeping the domain model decoupled from
 * the security framework.</p>
 *
 * @author Jiteen
 * @since 1.0
 * @see UserDetailsService
 * @see UserPrincipal
 * @see UserRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository used to query active user accounts from the database.
     *
     * <p>Only queries that filter on {@code deletedAt IS NULL} are issued so
     * that soft-deleted accounts cannot be authenticated.</p>
     */
    private final UserRepository userRepository;

    // ─── UserDetailsService ───────────────────────────────────────────────────

    /**
     * Locates an active user by their email address and returns a
     * {@link UserPrincipal} that Spring Security uses to perform credential
     * validation and authorisation checks.
     *
     * <p>The following steps are executed in order:</p>
     * <ol>
     *   <li>The supplied {@code username} (treated as an email address) is
     *       trimmed of leading and trailing whitespace and converted to lower
     *       case to produce a canonical lookup key.</li>
     *   <li>The {@link UserRepository} is queried for an active, non-deleted
     *       user matching the normalised email.</li>
     *   <li>If no match is found a {@link UsernameNotFoundException} is thrown.
     *       Spring Security's {@code DaoAuthenticationProvider} catches this
     *       and translates it to a generic authentication failure, preventing
     *       email-enumeration attacks.</li>
     *   <li>On success the {@link User} entity is adapted to a
     *       {@link UserPrincipal} and returned to the provider for credential
     *       and account-state validation.</li>
     * </ol>
     *
     * @param username the email address submitted by the client; must not be
     *                 {@code null}
     * @return a {@link UserPrincipal} representing the located active user
     * @throws UsernameNotFoundException if no active user exists with the
     *                                   supplied email address, or if the
     *                                   account has been soft-deleted
     */
    @Override
    @Cacheable(value = CacheNames.USERS, key = "#username.trim().toLowerCase()")
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {

        final String normalizedEmail = username.trim().toLowerCase();

        log.debug("Loading user details for [{}]", normalizedEmail);

        final User user = userRepository
                .findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseGet(() -> {
                    log.warn("No active user found with email [{}]", normalizedEmail);
                    throw new UsernameNotFoundException(
                            "User not found with email: " + normalizedEmail);
                });

        return UserPrincipal.fromUser(user);
    }

    /**
     * Removes the cached {@link UserDetails} entry for the given email address.
     *
     * <p>
     * Called by {@code AuthServiceImpl.logout()} after a refresh token is revoked
     * to ensure that the next request for this user triggers a fresh database lookup
     * rather than returning a potentially stale cached principal.
     * </p>
     *
     * @param email the normalized email address whose cache entry should be removed
     */
    @CacheEvict(value = CacheNames.USERS, key = "#email")
    public void evictUserCache(String email) {
        log.debug("Evicted user cache entry for [{}]", email);
    }
}