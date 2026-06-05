package com.jiteen.claims.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

/**
 * Data Transfer Object representing a user login request within the claims
 * authentication domain.
 *
 * <p>This DTO captures the credentials required to authenticate an existing user
 * and is validated using Jakarta Validation constraints at the API boundary. The
 * raw password is intentionally excluded from the generated {@link #toString()}
 * representation to prevent accidental exposure in logs.</p>
 *
 * @author Jiteen
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class LoginRequest implements Serializable {

    /**
     * The email address used to authenticate the user.
     *
     * <p>Must be non-blank and conform to a valid email format. This value serves
     * as the unique login identifier for the account.</p>
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * The raw password supplied by the user.
     *
     * <p>Must be non-blank. The value is verified against the securely hashed
     * password stored for the account and is never stored in plain text.</p>
     */
    @NotBlank(message = "Password must not be blank")
    private String password;
}