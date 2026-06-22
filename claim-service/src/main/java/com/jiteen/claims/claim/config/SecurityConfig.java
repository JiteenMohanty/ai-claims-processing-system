package com.jiteen.claims.claim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Enterprise security baseline configuration for the Claim Microservice.
 * <p>
 * This class establishes the foundational network protocol authorization rules,
 * filter chain mechanics, and security perimeter configurations using the
 * programmatic lambda structure required by Spring Security 6 and Spring Boot
 * 3.5 ecosystems.
 * </p>
 * <p>
 * Grounded within the platform's Phase 4 implementation milestones, this
 * configuration operates under a temporary open-access permutation to
 * streamline isolated workspace development, integration testing, endpoint
 * exploration, and automated local file-handling diagnostics. Cross-Site
 * Request Forgery (CSRF), standard form logins, and traditional HTTP Basic
 * access loops are explicitly bypassed while configuring an immutable stateless
 * execution strategy, creating a clean technical foundation for the structural
 * introduction of JWT validation layers in the next architecture phase.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the application's root web firewall filter topology and
     * request-matching regulations.
     * <p>
     * Explicitly registers white-listed parameters allowing unrestricted
     * ingress to interactive OpenAPI/Swagger endpoints, structural system
     * telemetry vectors (Spring Boot Actuator), and relative domain routing
     * points, while fallback configurations permit any remaining downstream
     * operational paths without authorization tokens.
     * </p>
     *
     * @param http the target {@link HttpSecurity} builder state container used
     * to map security interceptors
     * @return a fully constructed and compiled {@link SecurityFilterChain} bean
     * instance
     * @throws Exception if an unrecoverable structural assembly exception
     * occurs during filter orchestration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection since this microservice operates in a stateless REST API model
                .csrf(AbstractHttpConfigurer::disable)
                // Disable browser-oriented interface flows
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Enforce standard stateless microservice architecture session management policies
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define endpoint request authorization controls mapping to Phase 4 permissive boundaries
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()
                .anyRequest().permitAll()
                );

        return http.build();
    }
}
