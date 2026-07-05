package com.jiteen.claims.auth.config;

/**
 * Centralized registry of Redis cache name constants used within the Auth Service.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public final class CacheNames {

    private CacheNames() {}

    /**
     * Cache for {@link org.springframework.security.core.userdetails.UserDetails} lookups
     * keyed by normalized email address.
     *
     * <p>
     * Populated by {@code CustomUserDetailsService.loadUserByUsername()} which is
     * called on every authenticated HTTP request during JWT filter chain processing.
     * Caching this result significantly reduces per-request database load.
     * TTL: 30 minutes (configurable via {@code cache.ttl.users}).
     * </p>
     *
     * <p>
     * Entries are evicted programmatically on {@code logout()} to ensure that a
     * revoked session cannot use a cached {@code UserDetails} object.
     * </p>
     */
    public static final String USERS = "users";
}
