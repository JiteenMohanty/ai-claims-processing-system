package com.jiteen.claims.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a persisted refresh token within the claims
 * authentication domain.
 *
 * <p>A refresh token is issued to a user upon successful authentication and may
 * be exchanged for a new access token without requiring re-authentication. Each
 * token is associated with a single {@link User}, carries an expiration
 * timestamp, and may be explicitly revoked to invalidate it prior to its natural
 * expiry. This entity extends {@link AuditBaseEntity} to inherit standard audit
 * metadata.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true, of = "id")
@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_tokens_token", columnNames = "token")
        }
)
public class RefreshToken extends AuditBaseEntity {

    /**
     * The unique identifier (primary key) of the refresh token.
     *
     * <p>Generated automatically using a UUID strategy. This value is immutable
     * once assigned and uniquely identifies the token record.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * The opaque refresh token value.
     *
     * <p>Stored as a string and constrained to be unique across all records. This
     * value is presented by the client when requesting a new access token.</p>
     */
    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    /**
     * The user to whom this refresh token belongs.
     *
     * <p>Mapped as a lazily-loaded many-to-one association to avoid unnecessary
     * loading of the owning {@link User} unless explicitly accessed.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The instant at which this refresh token expires.
     *
     * <p>After this timestamp, the token is considered invalid and can no longer
     * be exchanged for a new access token.</p>
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Indicates whether this refresh token has been revoked.
     *
     * <p>When {@code true}, the token is invalidated and must not be honored,
     * even if it has not yet reached its expiration timestamp.</p>
     */
    @Column(name = "revoked", nullable = false)
    private Boolean revoked;
}