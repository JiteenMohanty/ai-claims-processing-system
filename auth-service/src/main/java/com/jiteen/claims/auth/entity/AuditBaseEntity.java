package com.jiteen.claims.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base entity that provides common auditing fields for all persistent
 * entities within the claims authentication domain.
 *
 * <p>This class is designed to be extended by concrete JPA entities and supplies
 * standardized lifecycle timestamps:</p>
 * <ul>
 *     <li>{@link #createdAt} – the moment the entity was first persisted.</li>
 *     <li>{@link #updatedAt} – the moment the entity was last modified.</li>
 *     <li>{@link #deletedAt} – the moment the entity was soft-deleted, if
 *         applicable.</li>
 * </ul>
 *
 * <p>The {@link #createdAt} and {@link #updatedAt} timestamps are managed
 * automatically through the {@link PrePersist} and {@link PreUpdate} lifecycle
 * callbacks. The {@link #deletedAt} timestamp is intended to support soft-delete
 * semantics and is managed by application-level logic.</p>
 *
 * <p>As a {@link MappedSuperclass}, this type contributes its mapping
 * information to subclasses but is not itself mapped to a database table.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class AuditBaseEntity implements Serializable {

    /**
     * Timestamp indicating when the entity was first persisted.
     *
     * <p>This value is populated automatically during the {@link PrePersist}
     * lifecycle callback and is immutable thereafter, as the column is declared
     * non-updatable.</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    /**
     * Timestamp indicating when the entity was last updated.
     *
     * <p>This value is populated automatically during both the
     * {@link PrePersist} and {@link PreUpdate} lifecycle callbacks.</p>
     */
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    /**
     * Timestamp indicating when the entity was soft-deleted.
     *
     * <p>A {@code null} value denotes an active (non-deleted) entity. This field
     * is managed by application-level soft-delete logic rather than JPA
     * lifecycle callbacks.</p>
     */
    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    /**
     * JPA lifecycle callback invoked immediately before the entity is first
     * persisted.
     *
     * <p>Initializes both {@link #createdAt} and {@link #updatedAt} to the
     * current system timestamp.</p>
     */
    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA lifecycle callback invoked immediately before an existing entity is
     * updated.
     *
     * <p>Refreshes {@link #updatedAt} to the current system timestamp while
     * leaving {@link #createdAt} unchanged.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}