package com.jiteen.claims.auth.security;

import com.jiteen.claims.auth.domain.entity.User;
import com.jiteen.claims.auth.domain.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security principal adapter for the {@link User} domain entity.
 *
 * <p>This class bridges the gap between the application's {@link User} entity
 * and the {@link UserDetails} contract required by Spring Security's
 * authentication and authorisation infrastructure. Rather than coupling the
 * {@link User} entity directly to the security framework, {@code UserPrincipal}
 * follows the Adapter pattern: it is constructed once per authentication event
 * via the {@link #fromUser(User)} factory method and carried through the
 * {@code SecurityContext} for the lifetime of the request.</p>
 *
 * <p>Account state is derived from {@link UserStatus} and governs the outcome
 * of Spring Security's pre-authentication checks:</p>
 * <ul>
 *   <li>{@link UserStatus#LOCKED} — {@link #isAccountNonLocked()} returns
 *       {@code false}, causing Spring Security to raise a
 *       {@code LockedException} before credentials are validated.</li>
 *   <li>{@link UserStatus#ACTIVE} or {@link UserStatus#PENDING_VERIFICATION}
 *       — {@link #isEnabled()} returns {@code true}; all other statuses are
 *       treated as disabled and will raise a {@code DisabledException}.</li>
 * </ul>
 *
 * <p>{@link #isAccountNonExpired()} and {@link #isCredentialsNonExpired()}
 * unconditionally return {@code true} because the current domain model does not
 * track account or credential expiry; these can be wired to real fields in a
 * future iteration without changing the public API.</p>
 *
 * @author Jiteen
 * @since 1.0
 * @see UserDetails
 * @see User
 * @see UserStatus
 */
@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the underlying user account.
     *
     * <p>Exposed via {@link #getId()} by Lombok's {@code @Getter} so that
     * application code can resolve the domain entity from the principal without
     * a second database query.</p>
     */
    private final UUID id;

    /**
     * The user's email address, used as the Spring Security username.
     *
     * <p>This value is returned by {@link #getUsername()} and must be unique
     * across all principals in the system.</p>
     */
    private final String email;

    /**
     * The BCrypt-hashed password for the user account.
     *
     * <p>Returned by {@link #getPassword()} and compared against the raw
     * credential supplied at login by Spring Security's
     * {@code DaoAuthenticationProvider}. The plain-text password is never
     * stored or surfaced by this class.</p>
     */
    private final String passwordHash;

    /**
     * The current lifecycle status of the user account.
     *
     * <p>Drives the boolean account-state methods required by
     * {@link UserDetails}. See the class-level documentation for the mapping
     * between {@link UserStatus} values and Spring Security behaviour.</p>
     */
    private final UserStatus status;

    /**
     * The set of granted authorities derived from the user's assigned role.
     *
     * <p>Populated at construction time by {@link #fromUser(User)} and
     * immutable thereafter. Spring Security's access-decision infrastructure
     * reads this collection to enforce role-based access control on protected
     * endpoints.</p>
     */
    private final Collection<? extends GrantedAuthority> authorities;

    // ─── Factory ─────────────────────────────────────────────────────────────

    /**
     * Creates a {@code UserPrincipal} from the given {@link User} domain entity.
     *
     * <p>The user's single {@link com.jiteen.claims.auth.domain.enums.Role} is
     * converted to a {@link SimpleGrantedAuthority} using the Spring Security
     * convention of prefixing the role name with {@code "ROLE_"}. For example,
     * {@code Role.ADMIN} becomes the authority {@code "ROLE_ADMIN"},
     * {@code Role.OFFICER} becomes {@code "ROLE_OFFICER"}, and {@code Role.USER}
     * becomes {@code "ROLE_USER"}.</p>
     *
     * @param user the {@link User} entity to adapt; must not be {@code null}
     * @return a fully initialised {@code UserPrincipal} reflecting the current
     *         state of the supplied entity
     */
    public static UserPrincipal fromUser(final User user) {

        final List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                authorities
        );
    }

    // ─── UserDetails ─────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Returns the email address as the Spring Security username. This value
     * is used as the lookup key by the application's
     * {@code UserDetailsService} implementation.</p>
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the BCrypt-hashed password. Spring Security's
     * {@code DaoAuthenticationProvider} compares this value against the
     * raw credential supplied at login using the configured
     * {@code PasswordEncoder}.</p>
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} for all accounts. Account expiry is not modelled
     * in the current domain; introduce an {@code expiresAt} field on the
     * {@link User} entity to enable time-bounded accounts in a future
     * iteration.</p>
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code false} when the account status is
     * {@link UserStatus#LOCKED}, causing Spring Security to raise a
     * {@code LockedException} during authentication. All other statuses are
     * treated as unlocked.</p>
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} for all accounts. Credential expiry is not
     * modelled in the current domain; introduce a {@code credentialsExpiresAt}
     * field on the {@link User} entity to enforce password rotation policies
     * in a future iteration.</p>
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when the account status is
     * {@link UserStatus#ACTIVE} or {@link UserStatus#PENDING_VERIFICATION}.
     * Accounts in any other state (e.g. suspended or deactivated) are treated
     * as disabled, and Spring Security will raise a {@code DisabledException}
     * during authentication.</p>
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE
                || status == UserStatus.PENDING_VERIFICATION;
    }
}