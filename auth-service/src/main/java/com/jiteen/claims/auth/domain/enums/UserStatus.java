package com.jiteen.claims.auth.domain.enums;

import java.io.Serializable;

/**
 * Represents the lifecycle status of a user account within the claims
 * authentication domain.
 *
 * <p>The status governs whether a user is permitted to authenticate and
 * interact with the system. It is evaluated by the authentication and
 * authorization layers to enforce account-level access policies.</p>
 *
 * <p>This enum is {@link Serializable} to support secure transmission and
 * persistence across distributed system boundaries, such as session storage,
 * caching layers, and remote service invocations.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public enum UserStatus implements Serializable {

    /**
     * Indicates that the user account is active and fully operational.
     *
     * <p>Users in this state are permitted to authenticate and access the
     * system in accordance with their assigned roles.</p>
     */
    ACTIVE,

    /**
     * Indicates that the user account is inactive and currently disabled.
     *
     * <p>Users in this state are not permitted to authenticate. The account may
     * be reactivated through an administrative or user-initiated process.</p>
     */
    INACTIVE,

    /**
     * Indicates that the user account has been locked.
     *
     * <p>Accounts are typically locked as a security measure, for example after
     * repeated failed authentication attempts. A locked account requires an
     * explicit unlock action before access can be restored.</p>
     */
    LOCKED,

    /**
     * Indicates that the user account has been created but is awaiting
     * verification.
     *
     * <p>Users in this state must complete the required verification process,
     * such as email or identity confirmation, before the account becomes
     * {@link #ACTIVE}.</p>
     */
    PENDING_VERIFICATION
}