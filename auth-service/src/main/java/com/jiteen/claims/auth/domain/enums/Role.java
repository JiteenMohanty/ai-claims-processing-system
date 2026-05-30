package com.jiteen.claims.auth.domain.enums;

import java.io.Serializable;

/**
 * Represents the set of roles available within the claims authentication domain.
 *
 * <p>Each role defines a distinct level of access and responsibility within the
 * system. Roles are used throughout the authorization layer to enforce
 * fine-grained access control and to determine the operations a given principal
 * is permitted to perform.</p>
 *
 * <p>This enum is {@link Serializable} to support secure transmission and
 * persistence across distributed system boundaries, such as session storage,
 * caching layers, and remote service invocations.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
public enum Role implements Serializable {

    /**
     * Administrative role with full, unrestricted access to the system.
     *
     * <p>Principals assigned this role are permitted to manage users, configure
     * system settings, and perform all operations available within the
     * application.</p>
     */
    ADMIN,

    /**
     * Operational role responsible for processing and managing insurance claims.
     *
     * <p>Principals assigned this role are permitted to review, approve, reject,
     * and otherwise administer claims, but do not possess full administrative
     * privileges.</p>
     */
    CLAIMS_OFFICER,

    /**
     * End-user role representing a customer of the claims platform.
     *
     * <p>Principals assigned this role are permitted to submit and track their
     * own claims, with access limited to resources they own.</p>
     */
    CUSTOMER
}