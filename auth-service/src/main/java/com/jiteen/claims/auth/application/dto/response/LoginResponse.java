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
 * user authentication within the claims authentication domain.
 *
 * <p>This DTO encapsulates the tokens and associated metadata issued to the
 * client upon successful login. It carries the access and refresh tokens
 * required for subsequent authenticated requests and token renewal. No
 * sensitive credential data, such as the password or password hash, is
 * exposed.</p>
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
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The JWT access token issued after successful authentication.
     *
     * <p>This token must be presented on subsequent requests to access
     * protected resources, typically via the {@code Authorization} header.</p>
     */
    private String accessToken;

    /**
     * The JWT refresh token used to obtain new access tokens.
     *
     * <p>When the access token expires, this token can be exchanged for a new
     * access token without requiring the user to re-authenticate.</p>
     */
    private String refreshToken;

    /**
     * The type of the issued token.
     *
     * <p>The expected value is {@code "Bearer"}, indicating that the access
     * token should be used as a bearer token in the {@code Authorization}
     * header.</p>
     */
    private String tokenType;

    /**
     * The access token expiration time, expressed in seconds.
     *
     * <p>Indicates the lifetime of the issued access token relative to the time
     * of issuance, after which the token is no longer valid.</p>
     */
    private Long expiresIn;
}