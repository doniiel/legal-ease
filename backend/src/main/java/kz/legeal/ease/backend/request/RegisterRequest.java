package kz.legeal.ease.backend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.Gender;
import lombok.Getter;

@Getter
public class RegisterRequest {

    private String firstName;

    private String middleName;

    private String lastName;

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

    private String iin;

    private String password;
}