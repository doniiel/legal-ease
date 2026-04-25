package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System-wide aggregate metrics for the admin dashboard")
public record SystemMetricsDto(
        @Schema(description = "Total registered (non-deleted) users") long totalUsers,
        @Schema(description = "Total active lawyers") long totalLawyers,
        @Schema(description = "Total document templates") long totalTemplates,
        @Schema(description = "Total non-deleted documents across all statuses") long totalDocuments,
        @Schema(description = "Lawyer applications awaiting review") long pendingLawyerApplications,
        @Schema(description = "Documents created in the current calendar month") long documentsThisMonth,
        @Schema(description = "Users with an active USER role") long activeUsers
) {}
