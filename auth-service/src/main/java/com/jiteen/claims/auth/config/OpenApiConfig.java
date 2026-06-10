package com.jiteen.claims.auth.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the claims authentication service.
 *
 * <p>This configuration registers the OpenAPI definition used to generate the
 * Swagger UI documentation. It also declares a {@code bearerAuth} security
 * scheme, enabling JWT bearer token authentication directly from the Swagger UI
 * via the "Authorize" dialog.</p>
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI claimsOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/")
                ));
    }
}