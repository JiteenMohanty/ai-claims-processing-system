package com.jiteen.claims.gateway.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JWT validation within the API Gateway.
 *
 * <p>These properties are bound from the {@code jwt} prefix in the application
 * configuration and provide the gateway with the shared secret and expected issuer
 * needed to verify tokens produced by the auth-service.</p>
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * jwt:
 *   secret: your-secret-key
 *   issuer: auth-service
 * }</pre>
 *
 * <p>The {@code secret} value is sensitive and must be supplied through a secure
 * mechanism (environment variables or a secrets manager) and must match the secret
 * configured in the auth-service.</p>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * The HMAC-SHA signing secret used to verify incoming JWTs.
     *
     * <p>Must be identical to the secret configured in the auth-service.
     * Should be at least 32 bytes (256 bits) for HS256 compliance.</p>
     */
    private String secret;

    /**
     * The expected issuer ({@code iss}) claim of incoming JWTs.
     *
     * <p>Tokens whose {@code iss} claim does not match this value will be rejected.</p>
     */
    private String issuer;
}
