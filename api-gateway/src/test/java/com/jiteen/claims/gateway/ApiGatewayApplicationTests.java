package com.jiteen.claims.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration smoke test verifying that the Spring application context loads successfully.
 *
 * <p>Spring Cloud Gateway is WebFlux-based; no additional reactive test configuration is
 * required. The default {@code application.yml} properties supply all required values,
 * including the JWT secret, so the context starts without any external services.</p>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@SpringBootTest
class ApiGatewayApplicationTests {

    /**
     * Verifies that the Spring application context loads without errors.
     *
     * <p>A failure here indicates a misconfiguration (missing required properties,
     * bean wiring issues, etc.) that must be resolved before the service can start.</p>
     */
    @Test
    void contextLoads() {
    }
}
