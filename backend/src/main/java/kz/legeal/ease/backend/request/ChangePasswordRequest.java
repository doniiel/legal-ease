package kz.legeal.ease.backend.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Reset code is required")
    @Min(value = 100000, message = "Reset code must be at least 6 digits")
    @Max(value = 999999, message = "Reset code must be at most 6 digits")
    private Integer resetCode;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;

}
