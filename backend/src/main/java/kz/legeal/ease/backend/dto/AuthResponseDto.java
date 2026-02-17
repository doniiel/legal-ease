package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuthResponseDto {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String accessToken;

    @Schema(description = "JWT Refresh Token", example = "dGhpc19pc19hX3JlZnJlc2hfdG9rZW4...")
    private String refreshToken;

    @Schema(description = "Expiration time in seconds", example = "3600")
    private long expiresIn;

    @Schema(description = "Server timestamp when the token was issued", example = "2025-09-04T12:34:56")
    private LocalDateTime timestamp;
}