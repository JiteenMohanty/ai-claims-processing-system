package com.jiteen.claims.auth.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Temporary REST controller used to verify that JWT-based authentication is
 * functioning correctly within the claims authentication domain.
 *
 * <p>
 * This controller exposes a single diagnostic endpoint that reflects the
 * currently authenticated principal as resolved from the Spring Security
 * context. It is intended for verification purposes and is not part of the
 * permanent public API surface.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "User", description = "User-related endpoints")
public class UserController {

    /**
     * Returns information about the currently authenticated user.
     *
     * <p>
     * The details are extracted from the {@link Authentication} held in the
     * {@link SecurityContextHolder}, including the principal's username,
     * granted authorities, and authentication state. This endpoint is primarily
     * intended to confirm that a presented JWT has been successfully validated
     * and that the security context has been populated.</p>
     *
     * @return a {@link ResponseEntity} with HTTP 200 containing the current
     * user's username, authorities, and authentication status
     */
    @Operation(
            summary = "Get current authenticated user",
            description = "Returns information about the currently authenticated user extracted from the Spring Security context."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Success"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized"
        )
    })
    @GetMapping(
            path = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        final List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        final Map<String, Object> response = Map.of(
                "username", authentication.getName(),
                "authorities", authorities,
                "authenticated", authentication.isAuthenticated()
        );

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> customerEndpoint() {
        return ResponseEntity.ok("Customer access granted");
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/claims-officer")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    public ResponseEntity<String> claimsOfficerEndpoint() {
        return ResponseEntity.ok("Claims officer access granted");
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Admin access granted");
    }

}
