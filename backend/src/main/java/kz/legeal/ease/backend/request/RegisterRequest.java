package kz.legeal.ease.backend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.Gender;
import lombok.Getter;

@Getter
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "IIN is required")
    @Size(min = 12, max = 12, message = "IIN must be exactly 12 digits")
    @Pattern(regexp = "\\d{12}", message = "IIN must contain only digits")
    private String iin;

    private Gender gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+7|8)\\d{10}$",
            message = "Phone must be valid (e.g. +77011234567 or 87011234567)"
    )
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain uppercase, lowercase and digit"
    )
    private String password;
}