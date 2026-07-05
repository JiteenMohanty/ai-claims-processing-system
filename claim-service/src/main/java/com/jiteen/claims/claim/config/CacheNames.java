package com.jiteen.claims.claim.config;

/**
 * Centralized registry of Redis cache name constants used within the Claim Service.
 *
 * <p>
 * Defining cache names as constants prevents typo-based bugs when the same name
 * must appear in both {@code @Cacheable} / {@code @CacheEvict} annotations and in
 * per-cache TTL configuration maps.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public final class CacheNames {

    private CacheNames() {}

    /**
     * Cache for individual claim lookups keyed by claim UUID.
     * TTL: 15 minutes (configurable via {@code cache.ttl.claims}).
     */
    public static final String CLAIMS = "claims";

    /**
     * Cache for the full list of active claims returned by {@code getAllClaims()}.
     * TTL: 5 minutes (configurable via {@code cache.ttl.claims-list}).
     * Shorter TTL because list results are broader and become stale faster
     * as new claims are created.
     */
    public static final String CLAIMS_LIST = "claims-list";
}
