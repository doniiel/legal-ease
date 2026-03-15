package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Response returned when a user creates a share link for a document. */
@Schema(description = "Time-limited public share link for a completed document")
public record DocumentShareResponse(
        @Schema(description = "Full public URL to access the shared document PDF") String shareUrl,
        @Schema(description = "Secure token embedded in the share URL") String token,
        @Schema(description = "When the share link expires") LocalDateTime expiresAt
) {}
