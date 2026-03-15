package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.AuditAction;

import java.time.LocalDateTime;

@Schema(description = "Audit log entry for compliance and tracing")
public record AuditLogDto(
        @Schema(description = "Log entry ID") Long id,
        @Schema(description = "ID of the user who triggered the action, or null for system events") Long userId,
        @Schema(description = "Action performed") AuditAction action,
        @Schema(description = "Entity type (e.g. 'Document', 'Template')") String entityType,
        @Schema(description = "Entity primary key") Long entityId,
        @Schema(description = "Optional JSON metadata") String metadata,
        @Schema(description = "When the event occurred") LocalDateTime createdAt
) {}
