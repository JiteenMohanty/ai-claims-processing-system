package com.jiteen.claims.auth.application.service.jwt;

import com.jiteen.claims.auth.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jsonwebtoken.io.DecodingException;

/**
 * Service responsible for the creation, parsing, and validation of JSON Web
 * Tokens (JWTs) within the claims authentication domain.
 *
 * <p>
 * This service encapsulates all interactions with the underlying JJWT library
 * and centralizes token-related concerns such as signing, claim extraction, and
 * validation. It derives its signing key and token lifetimes from
 * {@link JwtProperties}, enabling environment-specific configuration without
 * code changes.</p>
 *
 * <p>
 * Both access and refresh tokens are signed using an HMAC-SHA key derived from
 * the configured secret. Access tokens additionally carry role and status
 * claims used for authorization decisions.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /**
     * Claim key holding the user's role within an access token.
     */
    private static final String CLAIM_ROLE = "role";

    /**
     * Claim key holding the user's account status within an access token.
     */
    private static final String CLAIM_STATUS = "status";

    /**
     * Pattern used to parse human-readable duration strings such as
     * {@code "15m"} or {@code "7d"}.
     */
    private static final Pattern DURATION_PATTERN
            = Pattern.compile("^(\\d+)\\s*([smhd])$", Pattern.CASE_INSENSITIVE);

    /**
     * Externalized JWT configuration including issuer, secret, and expirations.
     */
    private final JwtProperties jwtProperties;

    /**
     * The lazily-resolved signing key derived from the configured secret.
     */
    private final SecretKey signingKey;

    /**
     * Constructs a new {@code JwtService} using constructor injection.
     *
     * <p>
     * The HMAC-SHA signing key is derived once during construction from the
     * configured secret to avoid repeated key derivation on every
     * operation.</p>
     *
     * @param jwtProperties the JWT configuration properties; must not be
     * {@code null}
     */
    public JwtService(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = buildSigningKey(jwtProperties.getSecret());
    }

    /**
     * Generates a signed JWT access token for the given user.
     *
     * <p>
     * The token uses the user's email as its subject and embeds the role and
     * status as custom claims. The issuer, issued-at, and expiration values are
     * populated from configuration and the current time.</p>
     *
     * @param email the user's email, used as the token subject
     * @param role the user's role, embedded as the {@code role} claim
     * @param status the user's account status, embedded as the {@code status}
     * claim
     * @return a compact, signed JWT access token
     */
    public String generateAccessToken(final String email, final String role, final String status) {
        log.debug("Generating access token for subject [{}]", email);
        final Duration ttl = parseDuration(jwtProperties.getAccessTokenExpiration());
        return buildToken(email, ttl, Map.of(CLAIM_ROLE, role, CLAIM_STATUS, status));
    }

    /**
     * Generates a signed JWT refresh token for the given user.
     *
     * <p>
     * The refresh token carries no authorization claims; it only identifies the
     * subject and is used to obtain new access tokens after expiration.</p>
     *
     * @param email the user's email, used as the token subject
     * @return a compact, signed JWT refresh token
     */
    public String generateRefreshToken(final String email) {
        log.debug("Generating refresh token for subject [{}]", email);
        final Duration ttl = parseDuration(jwtProperties.getRefreshTokenExpiration());
        return buildToken(email, ttl, Map.of());
    }

    /**
     * Extracts the subject (username/email) from the supplied token.
     *
     * @param token the compact JWT string
     * @return the subject claim of the token
     * @throws JwtException if the token cannot be parsed or verified
     */
    public String extractUsername(final String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates the supplied token for signature integrity, issuer correctness,
     * and expiration.
     *
     * <p>
     * This method does not throw on validation failure; instead it logs the
     * cause at an appropriate level and returns {@code false}.</p>
     *
     * @param token the compact JWT string to validate
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateToken(final String token) {
        try {
            final Claims claims = extractAllClaims(token);

            final String tokenIssuer = claims.getIssuer();
            if (!jwtProperties.getIssuer().equals(tokenIssuer)) {
                log.warn("Token validation failed: unexpected issuer [{}]", tokenIssuer);
                return false;
            }

            // Parsing already enforces signature integrity and rejects expired tokens.
            log.debug("Token successfully validated for subject [{}]", claims.getSubject());
            return true;
        } catch (final JwtException | IllegalArgumentException ex) {
            log.debug("Token validation failed", ex);
            return false;
        }
    }

    /**
     * Parses the supplied token and returns all of its claims.
     *
     * <p>
     * Parsing verifies the token signature against the configured signing key
     * and rejects expired or malformed tokens.</p>
     *
     * @param token the compact JWT string
     * @return the parsed {@link Claims} payload
     * @throws JwtException if the token is invalid, expired, or has an invalid
     * signature
     */
    public Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds a signed JWT for the given subject, time-to-live, and extra
     * claims.
     *
     * @param subject the token subject
     * @param ttl the duration the token remains valid
     * @param extraClaims any additional claims to embed; may be empty
     * @return the compact, signed JWT string
     */
    private String buildToken(final String subject, final Duration ttl, final Map<String, ?> extraClaims) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(ttl);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(subject)
                .claims(extraClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Derives the HMAC-SHA signing key from the configured secret.
     *
     * <p>
     * The secret is interpreted as a Base64-encoded value when possible;
     * otherwise its raw UTF-8 bytes are used. The resulting key length must
     * satisfy the minimum requirements of the HMAC-SHA algorithm.</p>
     *
     * @param secret the configured signing secret
     * @return a {@link SecretKey} suitable for signing and verification
     */
    private SecretKey buildSigningKey(final String secret) {

        final byte[] keyBytes;
        byte[] decoded;
        try {
    decoded = Decoders.BASE64.decode(secret);
} catch (DecodingException ex) {
    decoded = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
}
        keyBytes = decoded;
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes long"
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parses a human-readable duration string into a {@link Duration}.
     *
     * <p>
     * Supported suffixes are {@code s} (seconds), {@code m} (minutes),
     * {@code h} (hours), and {@code d} (days). Examples: {@code "15m"},
     * {@code "7d"}.</p>
     *
     * @param value the duration string to parse
     * @return the parsed {@link Duration}
     * @throws IllegalArgumentException if the value is null, blank, or
     * malformed
     */
    private Duration parseDuration(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token expiration value must not be null or blank");
        }

        final Matcher matcher = DURATION_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid token expiration format: " + value);
        }

        final long amount = Long.parseLong(matcher.group(1));
        final String unit = matcher.group(2).toLowerCase();

        return switch (unit) {
            case "s" ->
                Duration.of(amount, ChronoUnit.SECONDS);
            case "m" ->
                Duration.of(amount, ChronoUnit.MINUTES);
            case "h" ->
                Duration.of(amount, ChronoUnit.HOURS);
            case "d" ->
                Duration.of(amount, ChronoUnit.DAYS);
            default ->
                throw new IllegalArgumentException("Unsupported duration unit: " + unit);
        };
    }
}
