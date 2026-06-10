package com.jiteen.claims.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing a token refresh request within the claims
 * authentication domain.
 *
 * <p>This DTO carries the refresh token required to obtain a new access token
 * and is validated using Jakarta Validation constraints at the API boundary. The
 * refresh token is intentionally excluded from the generated {@link #toString()}
 * representation to prevent accidental exposure in logs.</p>
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
public class RefreshTokenRequest {

    /**
     * The refresh token previously issued to the user.
     *
     * <p>Must be non-blank. It is exchanged for a new access token without
     * requiring the user to re-authenticate and is never logged in plain
     * text.</p>
     */
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}