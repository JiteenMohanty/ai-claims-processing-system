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

/**
 * Data Transfer Object representing a user registration request within the
 * claims authentication domain.
 *
 * <p>This DTO captures the information required to register a new user and is
 * validated using Jakarta Validation constraints at the API boundary. The raw
 * password is intentionally excluded from the generated {@link #toString()}
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
public class RegisterRequest {

    /**
     * The email address of the user to register.
     *
     * <p>Must be non-blank and conform to a valid email format. This value also
     * serves as the unique login identifier for the account.</p>
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * The raw password chosen by the user.
     *
     * <p>Must be non-blank and between 8 and 100 characters in length. The value
     * is securely hashed before persistence and is never stored in plain
     * text.</p>
     */
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * The user's given (first) name.
     *
     * <p>Must be non-blank and not exceed 100 characters.</p>
     */
    @NotBlank(message = "First name must not be blank")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * The user's family (last) name.
     *
     * <p>Must be non-blank and not exceed 100 characters.</p>
     */
    @NotBlank(message = "Last name must not be blank")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
}