package kz.legeal.ease.backend.controller.openApi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.AuthResponseDto;
import kz.legeal.ease.backend.request.*;
import kz.legeal.ease.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-api/auth")
@Tag(
        name = "Authentication API",
        description = "Public endpoints for authentication, registration and password management"
)
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns access and refresh tokens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Register new user",
            description = "Registers a new user in the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            )
            @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Confirm user account",
            description = "Confirms user registration using verification code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification code")
    })
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Verification request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerificationRequest.class))
            )
            @RequestBody @Valid VerificationRequest request
    ) {
        authService.confirm(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Send reset password code",
            description = "Send verification code to user for resetting password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset code sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email address")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reset password request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class))
            )
            @RequestBody ResetPasswordRequest request
    ) {
        authService.sendResetPasswordCode(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Change password",
            description = "Changes user password using verification code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification code or password")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Change password request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordRequest.class))
            )
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "User logout",
            description = "Invalidates refresh token and logs user out"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid logout request")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Logout request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LogoutRequest.class))
            )
            @RequestBody @Valid LogoutRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}