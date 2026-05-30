package com.jiteen.claims.auth.domain.entity;

import com.jiteen.claims.auth.domain.enums.Role;
import com.jiteen.claims.auth.domain.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user account within the claims authentication domain.
 *
 * <p>This entity is mapped to the {@code users} table and encapsulates the
 * credentials, profile information, role assignment, and lifecycle status of a
 * user. It extends {@link AuditBaseEntity} to inherit standardized auditing
 * timestamps.</p>
 *
 * <p>Sensitive information such as the password hash is intentionally excluded
 * from the generated {@link #toString()} representation to prevent accidental
 * exposure in logs.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, of = "id")
@ToString(callSuper = true, exclude = "passwordHash")
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User extends AuditBaseEntity {

    /**
     * Unique identifier for the user, generated as a UUID.
     *
     * <p>This value serves as the primary key and is assigned automatically by
     * the persistence provider upon creation.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * The user's email address, used as the unique login identifier.
     *
     * <p>This value must be unique across all users and is constrained by the
     * {@code uk_users_email} unique constraint.</p>
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * The securely hashed representation of the user's password.
     *
     * <p>The raw password is never stored. This field holds only the result of
     * a strong, one-way hashing algorithm.</p>
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * The user's given (first) name.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * The user's family (last) name.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * The role assigned to the user, determining their access level.
     *
     * <p>Persisted as a {@link String} to preserve readability and stability of
     * the stored value across deployments.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    /**
     * The current lifecycle status of the user account.
     *
     * <p>Persisted as a {@link String} to preserve readability and stability of
     * the stored value across deployments.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private UserStatus status;

    /**
     * Timestamp of the user's most recent successful authentication.
     *
     * <p>A {@code null} value indicates that the user has never logged in.</p>
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}