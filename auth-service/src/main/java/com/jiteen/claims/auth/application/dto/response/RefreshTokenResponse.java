package com.jiteen.claims.auth.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Data Transfer Object representing the response returned after a successful
 * access token refresh within the claims authentication domain.
 *
 * <p>This DTO encapsulates the newly issued access token and its associated
 * metadata, allowing a client to continue making authenticated requests without
 * re-authenticating. The response also includes a new refresh token that supersedes the one
 * submitted with the request.</p>
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
public class RefreshTokenResponse implements Serializable {

    /**
     * The newly issued JWT access token.
     *
     * <p>This token must be presented on subsequent requests to access protected
     * resources, typically via the {@code Authorization} header.</p>
     */
    private String accessToken;

    /**
     * The type of the issued token.
     *
     * <p>The expected value is {@code "Bearer"}, indicating that the access token
     * should be used as a bearer token in the {@code Authorization} header.</p>
     */
    private String tokenType;

    /**
     * The access token lifetime, expressed in seconds.
     *
     * <p>Indicates the duration for which the newly issued access token remains
     * valid relative to the time of issuance, after which the token is no longer
     * accepted.</p>
     */
    private Long expiresIn;

    /**
     * The newly issued refresh token that supersedes the one submitted with
     * the refresh request.
     *
     * <p>Token rotation is applied on every successful refresh operation: the
     * previously issued refresh token is revoked server-side and replaced by
     * this value. The client must discard the old refresh token immediately and
     * persist this one instead, using it for all subsequent refresh operations.
     * Presenting a rotated-out token will result in a rejected request.</p>
     *
     * <p>As with all refresh tokens, this value is sensitive and must be stored
     * securely on the client (e.g. in an {@code HttpOnly} cookie or equivalent
     * secure storage) and never logged in plain text.</p>
     */
    private String refreshToken;
}