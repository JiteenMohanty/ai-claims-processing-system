package com.jiteen.claims.gateway.filter;

import com.jiteen.claims.gateway.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global gateway filter that enforces JWT-based authentication on all non-public routes.
 *
 * <p>The filter runs at order {@code -100}, after the {@link CorrelationIdFilter}
 * (order {@code -200}), so a correlation ID is always available when authentication
 * decisions are logged.</p>
 *
 * <p>Public paths (configured in {@link #PUBLIC_PATHS}) bypass authentication entirely.
 * All other paths require a valid {@code Bearer} token in the {@code Authorization}
 * header. When a valid token is found the filter enriches the downstream request with
 * two additional headers:</p>
 * <ul>
 *   <li>{@code X-User-Email} — the JWT subject (the authenticated user's email)</li>
 *   <li>{@code X-User-Role} — the {@code role} claim extracted from the token</li>
 * </ul>
 *
 * <p>If the token is missing or invalid the filter short-circuits the chain and writes
 * a {@code 401 Unauthorized} JSON response directly to the client.</p>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * Request path prefixes that are exempt from JWT authentication.
     *
     * <p>Requests whose path starts with any of these prefixes are forwarded to the
     * downstream service without a token being required.</p>
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/actuator"
    );

    /** JSON error body template for 401 responses. */
    private static final String UNAUTHORIZED_BODY =
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or missing authentication token\"}";

    private final JwtProperties jwtProperties;

    /**
     * Applies JWT authentication to the incoming request.
     *
     * <p>If the request path matches a public prefix the filter delegates immediately
     * to the next filter in the chain. Otherwise it extracts and validates the Bearer
     * token, enriches the request with user context headers on success, or writes a
     * {@code 401} error response on failure.</p>
     *
     * @param exchange the current server web exchange
     * @param chain    the remaining filter chain
     * @return a {@link Mono} that completes when request handling is finished
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            log.debug("Public path detected, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return writeUnauthorizedResponse(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = parseToken(token);
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            log.debug("JWT validated for subject='{}', role='{}', path='{}'", subject, role, path);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HEADER_USER_EMAIL, subject != null ? subject : "")
                    .header(HEADER_USER_ROLE, role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed for path '{}': {}", path, ex.getMessage());
            return writeUnauthorizedResponse(exchange);
        }
    }

    /**
     * Returns the order of this filter in the global filter chain.
     *
     * <p>Order {@code -100} places this filter after the {@link CorrelationIdFilter}
     * (order {@code -200}) and before the default Spring Cloud Gateway filters.</p>
     *
     * @return {@code -100}
     */
    @Override
    public int getOrder() {
        return -100;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given request path starts with any configured
     * public path prefix.
     *
     * @param path the URI path of the incoming request
     * @return {@code true} when authentication should be skipped
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Parses and validates the supplied JWT string using the configured HMAC secret.
     *
     * @param token the raw JWT (without the {@code Bearer } prefix)
     * @return the validated {@link Claims} extracted from the token
     * @throws JwtException             if the token is expired, malformed, or has an
     *                                  invalid signature
     * @throws IllegalArgumentException if the token string is blank or null
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Writes a {@code 401 Unauthorized} JSON response and completes the exchange
     * without delegating to the downstream filter chain.
     *
     * @param exchange the current server web exchange
     * @return a {@link Mono} that completes after the response has been written
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
