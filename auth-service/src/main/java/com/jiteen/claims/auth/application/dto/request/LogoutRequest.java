package com.jiteen.claims.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing a logout request within the claims
 * authentication domain.
 *
 * <p>This DTO carries the refresh token required to revoke the user's active
 * session during logout. It is validated using Jakarta Validation constraints
 * at the API boundary, ensuring the token is present and non-blank before any
 * revocation logic is invoked. The refresh token is intentionally excluded from
 * the generated {@link #toString()} representation to prevent accidental
 * exposure in application logs or diagnostic output.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "refreshToken")
public class LogoutRequest {

    /**
     * The refresh token previously issued to the user.
     *
     * <p>Must be non-blank. During logout this token is submitted to the
     * authentication service so that the corresponding session can be
     * invalidated and the token revoked, preventing any further use for
     * obtaining new access tokens. It is never logged in plain text.</p>
     */
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}