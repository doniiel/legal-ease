package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.SystemMetricsDto;
import kz.legeal.ease.backend.service.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "System Metrics (Admin)")
@RequiredArgsConstructor
public class MetricsController {

    private final AdminMetricsService metricsService;

    @Operation(summary = "Get system metrics (ADMIN only)")
    @GetMapping
    public ResponseEntity<SystemMetricsDto> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}
