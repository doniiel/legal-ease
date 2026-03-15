package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.SystemMetricsDto;
import kz.legeal.ease.backend.service.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metrics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - System Metrics", description = "Aggregate system-wide statistics for the admin dashboard")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final AdminMetricsService metricsService;

    @Operation(
            summary = "Get system metrics",
            description = "Returns aggregate counts: users, lawyers, templates, documents by status, pending applications, and total audit log entries."
    )
    @ApiResponse(responseCode = "200", description = "Metrics returned")
    @GetMapping
    public ResponseEntity<SystemMetricsDto> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}
