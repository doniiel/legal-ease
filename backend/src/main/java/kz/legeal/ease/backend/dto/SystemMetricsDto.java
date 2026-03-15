package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System-wide aggregate metrics for the admin dashboard")
public record SystemMetricsDto(
        @Schema(description = "Total registered (non-deleted) users") long totalUsers,
        @Schema(description = "Total active lawyers") long totalLawyers,
        @Schema(description = "Total document templates") long totalTemplates,
        @Schema(description = "Documents currently in DRAFT status") long draftDocuments,
        @Schema(description = "Documents in VALIDATED status (ready to complete)") long validatedDocuments,
        @Schema(description = "Documents in COMPLETED status") long completedDocuments,
        @Schema(description = "Documents in ARCHIVED status") long archivedDocuments,
        @Schema(description = "Total pending lawyer applications") long pendingApplications,
        @Schema(description = "Total audit log entries") long totalAuditLogs
) {}
