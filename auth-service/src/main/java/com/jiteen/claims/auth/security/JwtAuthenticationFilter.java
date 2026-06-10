package com.jiteen.claims.auth.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jiteen.claims.auth.application.service.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security filter responsible for authenticating incoming requests based
 * on a JSON Web Token (JWT) carried in the {@code Authorization} header.
 *
 * <p>
 * This filter executes once per request and inspects the {@code Authorization}
 * header for a bearer token. When a valid token is present, the associated
 * username and role are extracted and used to populate the
 * {@link SecurityContextHolder} with an authenticated principal. Requests
 * without a token, or with an invalid token, are allowed to proceed without
 * authentication so that downstream components (for example, authorization
 * rules) can determine the appropriate response.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * The HTTP header carrying the bearer token.
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * The required prefix for bearer tokens within the authorization header.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Service used to validate tokens and extract their claims.
     */
    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructs a new {@code JwtAuthenticationFilter} using constructor
     * injection.
     *
     * @param jwtService the service used for token validation and claim
     * extraction; must not be {@code null}
     */
    public JwtAuthenticationFilter(
            final JwtService jwtService,
            final CustomUserDetailsService customUserDetailsService) {

        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Inspects the incoming request for a bearer token and, when valid,
     * establishes the security context with the resolved principal and
     * authority.
     *
     * <p>
     * The filter chain is always continued. Authentication is only populated
     * when a valid token is present; otherwise the request proceeds
     * unauthenticated.</p>
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the filter chain to delegate to
     * @throws ServletException if the request cannot be processed
     * @throws IOException if an I/O error occurs during processing
     */
    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorizationHeader)) {
            log.trace("No Authorization header present; continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.trace("Authorization header does not use the Bearer scheme; continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.validateToken(token)) {
            log.debug("Invalid JWT token received; continuing filter chain unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        final String username = jwtService.extractUsername(token);

        if (StringUtils.hasText(username)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            final UserDetails userDetails
                    = customUserDetailsService.loadUserByUsername(username);

            final UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug(
                    "Authenticated user [{}] with authorities {}",
                    username,
                    userDetails.getAuthorities());
        }

        filterChain.doFilter(request, response);
    }
}
