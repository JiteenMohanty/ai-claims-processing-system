package com.jiteen.claims.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jiteen.claims.auth.domain.entity.RefreshToken;

/**
 * Repository providing persistence operations for refresh tokens.
 *
 * <p>This repository is responsible for retrieving and managing refresh token
 * records used during token renewal, revocation, and logout workflows.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token by its token value.
     *
     * @param token the refresh token value
     * @return the matching refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

}