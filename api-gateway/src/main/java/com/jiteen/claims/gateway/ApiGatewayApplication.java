package com.jiteen.claims.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the API Gateway service.
 *
 * <p>The API Gateway is the single ingress point for all external traffic directed at
 * the AI Claims Processing Platform. It is responsible for:</p>
 * <ul>
 *   <li>Routing requests to downstream microservices (auth-service, claim-service)</li>
 *   <li>JWT-based authentication enforcement via {@link com.jiteen.claims.gateway.filter.JwtAuthenticationFilter}</li>
 *   <li>Correlation ID propagation via {@link com.jiteen.claims.gateway.filter.CorrelationIdFilter}</li>
 *   <li>CORS policy enforcement</li>
 * </ul>
 *
 * <p>Built on Spring Cloud Gateway (Project Reactor / WebFlux), this service is
 * fully non-blocking and event-driven.</p>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to the Spring application context
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
