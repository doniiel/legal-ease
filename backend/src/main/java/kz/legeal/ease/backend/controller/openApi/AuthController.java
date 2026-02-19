package kz.legeal.ease.backend.controller.openApi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.request.*;
import kz.legeal.ease.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-api/auth")
@Tag(
        name = "Authentication API",
        description = "Endpoints for user authentication and account management"
)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully logged in",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Register new user",
            description = "Registers a new user in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Confirm user account",
            description = "Confirm user registration with verification code"
    )
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody @Valid VerificationRequest request) {
        authService.confirm(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Send reset password code",
            description = "Send verification code to user for resetting password"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.sendResetPasswordCode(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Change password",
            description = "Change password using verification code"
    )
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}
