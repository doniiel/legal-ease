package kz.legeal.ease.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised API error envelope returned for all error responses.
 *
 * <pre>
 * {
 *   "timestamp": "2026-03-15T12:00:00",
 *   "status":    400,
 *   "error":     "VALIDATION_001",
 *   "message":   "Validation failed",
 *   "path":      "/api/user/documents",
 *   "requestId": "a3f9...",
 *   "fieldErrors": [ { "field": "title", "message": "must not be blank" } ]
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response envelope")
public class ErrorResponseDto {

    @Schema(description = "ISO-8601 timestamp of when the error occurred")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private final int status;

    @Schema(description = "Machine-readable error code", example = "VALIDATION_001")
    private final String error;

    @Schema(description = "Human-readable description", example = "Validation failed")
    private final String message;

    @Schema(description = "Request URI that triggered the error", example = "/api/user/documents")
    private final String path;

    @Schema(description = "Correlation ID — from X-Request-ID header or auto-generated")
    private final String requestId;

    @Schema(description = "Per-field errors (only present for 400 validation failures)")
    private final List<FieldError> fieldErrors;

    @Getter
    @Builder
    @Schema(description = "Individual field validation error")
    public static class FieldError {

        @Schema(description = "Field name", example = "email")
        private final String field;

        @Schema(description = "Validation message", example = "must be a valid email address")
        private final String message;
    }
}
