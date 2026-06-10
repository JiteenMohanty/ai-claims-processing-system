package com.jiteen.claims.auth.config;

import com.jiteen.claims.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for the claims authentication domain.
 *
 * <p>This configuration establishes a stateless, JWT-based security model. CSRF
 * protection and HTTP Basic authentication are disabled, as authentication is
 * driven entirely by bearer tokens validated by the
 * {@link JwtAuthenticationFilter}. Public endpoints (authentication operations,
 * API documentation, and actuator endpoints) are exempt from authentication,
 * while all other endpoints require a valid token.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Endpoints that are publicly accessible without authentication.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/**"
    };

    /**
     * Filter responsible for authenticating requests based on a JWT bearer token.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs a new {@code SecurityConfig} using constructor injection.
     *
     * @param jwtAuthenticationFilter the JWT authentication filter to register in
     *                                the security filter chain; must not be {@code null}
     */
    public SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Defines the application's security filter chain.
     *
     * <p>The configuration disables CSRF protection and HTTP Basic
     * authentication, enforces a stateless session policy, permits access to the
     * designated public endpoints, and requires authentication for all other
     * requests. The {@link JwtAuthenticationFilter} is registered before the
     * {@link UsernamePasswordAuthenticationFilter} so that token-based
     * authentication is established prior to the standard authentication
     * processing.</p>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the fully configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides the password encoder used for hashing and verifying user
     * passwords.
     *
     * <p>A {@link BCryptPasswordEncoder} is used, applying an adaptive one-way
     * hashing function suitable for securely storing credentials.</p>
     *
     * @return the configured {@link PasswordEncoder}
     */

    /**
     * Exposes the application's {@link AuthenticationManager} as a Spring bean.
     *
     * <p>Spring Security does not register an {@link AuthenticationManager} in
     * the application context by default when a custom {@code SecurityFilterChain}
     * is used. This bean declaration bridges that gap, allowing the
     * {@link AuthenticationManager} to be injected into application components
     * — most notably {@code AuthServiceImpl} — that need to programmatically
     * authenticate credentials (e.g. during the login flow) without coupling
     * those components to the internal configuration machinery.</p>
     *
     * <p>The instance is sourced from
     * {@link AuthenticationConfiguration#getAuthenticationManager()}, which
     * assembles the manager from the security context's configured
     * {@link org.springframework.security.core.userdetails.UserDetailsService}
     * and {@link org.springframework.security.crypto.password.PasswordEncoder}
     * beans — in this codebase {@code CustomUserDetailsService} and the
     * BCrypt-backed encoder respectively.</p>
     *
     * @param authenticationConfiguration the auto-configured
     *        {@link AuthenticationConfiguration} supplied by Spring Security;
     *        must not be {@code null}
     * @return the fully initialised {@link AuthenticationManager} for this
     *         application context
     * @throws Exception if the {@link AuthenticationManager} cannot be built,
     *         as declared by
     *         {@link AuthenticationConfiguration#getAuthenticationManager()}
     * @since 1.0
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
 
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}