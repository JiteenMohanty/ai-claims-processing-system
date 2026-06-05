package com.jiteen.claims.auth.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * Type-safe configuration properties for JSON Web Token (JWT) handling within
 * the claims authentication domain.
 *
 * <p>These properties are bound from the {@code jwt} prefix in the application
 * configuration and centralize all JWT-related settings, including the token
 * issuer, signing secret, and token expiration policies. Externalizing these
 * values allows them to be tuned per environment without code changes.</p>
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * jwt:
 *   issuer: auth-service
 *   secret: some-secret
 *   access-token-expiration: 15m
 *   refresh-token-expiration: 7d
 * }</pre>
 *
 * <p>The {@code secret} value is sensitive and should be supplied through a
 * secure mechanism (for example, environment variables or a secrets manager)
 * rather than being committed to source control.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The issuer ({@code iss}) claim value to embed in generated tokens.
     *
     * <p>Identifies the principal that issued the JWT and may be validated by
     * consumers to ensure tokens originate from a trusted source.</p>
     */
    private String issuer;

    /**
     * The secret key used to sign and verify JWTs.
     *
     * <p>This value is sensitive and must be kept confidential. It should be
     * provided through a secure configuration source and never logged or
     * exposed.</p>
     */
    private String secret;

    /**
     * The access token expiration duration.
     *
     * <p>Expressed as a human-readable duration string (for example,
     * {@code "15m"}) that defines how long an issued access token remains
     * valid.</p>
     */
    private String accessTokenExpiration;

    /**
     * The refresh token expiration duration.
     *
     * <p>Expressed as a human-readable duration string (for example,
     * {@code "7d"}) that defines how long an issued refresh token remains valid
     * for obtaining new access tokens.</p>
     */
    private String refreshTokenExpiration;
}