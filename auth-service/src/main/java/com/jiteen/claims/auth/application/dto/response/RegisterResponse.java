package com.jiteen.claims.auth.application.dto.response;

import com.jiteen.claims.auth.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Data Transfer Object representing the response returned after a successful
 * user registration within the claims authentication domain.
 *
 * <p>This DTO exposes only non-sensitive information about the newly created
 * user. Credential-related data, such as the password or password hash, is
 * intentionally excluded.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegisterResponse {

    /**
     * The unique identifier assigned to the newly registered user.
     */
    private UUID userId;

    /**
     * The email address associated with the registered user account.
     */
    private String email;

    /**
     * The current lifecycle status of the registered user account.
     */
    private UserStatus status;
}