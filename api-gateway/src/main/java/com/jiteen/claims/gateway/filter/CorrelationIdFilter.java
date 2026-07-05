package com.jiteen.claims.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global gateway filter that ensures every request carries a unique correlation ID.
 *
 * <p>The correlation ID is used for distributed tracing: it allows log entries from
 * the gateway and all downstream services to be correlated back to a single
 * originating request. The filter runs at order {@code -200}, before the
 * {@link JwtAuthenticationFilter} (order {@code -100}), so all subsequent filters
 * and downstream services always see the header.</p>
 *
 * <p>Behaviour:</p>
 * <ol>
 *   <li>If the incoming request already contains an {@code X-Correlation-Id} header
 *       the existing value is reused (supports end-to-end tracing from trusted
 *       upstream callers).</li>
 *   <li>If the header is absent a new random UUID is generated.</li>
 *   <li>The resolved correlation ID is injected into both the outgoing request
 *       (forwarded to the downstream service) and the outgoing response (returned
 *       to the caller).</li>
 * </ol>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    /** Header name used to propagate the correlation ID. */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Ensures a correlation ID is present on both the downstream request and the
     * upstream response.
     *
     * @param exchange the current server web exchange
     * @param chain    the filter chain to delegate to after this filter
     * @return a {@link Mono} that completes when the exchange has been handled
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {}", correlationId);
        } else {
            log.debug("Reusing incoming correlation ID: {}", correlationId);
        }

        final String resolvedCorrelationId = correlationId;

        // Mutate the outgoing (downstream) request to include the header
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, resolvedCorrelationId)
                .build();

        // Add the correlation ID to the response so callers can reference it
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Schedule header on the response before it is committed — setting it after
        // chain.filter() completes is too late and throws UnsupportedOperationException
        // on the ReadOnlyHttpHeaders, causing Netty to close the connection mid-stream
        // (ERR_INCOMPLETE_CHUNKED_ENCODING on the client side).
        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, resolvedCorrelationId);
            return Mono.empty();
        });

        return chain.filter(mutatedExchange);
    }

    /**
     * Returns the order of this filter in the global filter chain.
     *
     * <p>Order {@code -200} places this filter before the JWT authentication filter
     * (order {@code -100}), guaranteeing that a correlation ID is available to all
     * subsequent filters and downstream services.</p>
     *
     * @return {@code -200}
     */
    @Override
    public int getOrder() {
        return -200;
    }
}
