package com.jiteen.claims.auth.application.dto.response;

import com.jiteen.claims.auth.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing the authenticated user's profile.
 *
 * <p>Returned by the {@code GET /api/v1/auth/profile} endpoint. Exposes
 * non-sensitive profile fields only — the password hash is never included.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /** The user's unique identifier. */
    private UUID id;

    /** The user's email address. */
    private String email;

    /** The user's given name. */
    private String firstName;

    /** The user's family name. */
    private String lastName;

    /** The role assigned to the user. */
    private Role role;

    /** Timestamp when the account was created. */
    private LocalDateTime createdAt;
}
