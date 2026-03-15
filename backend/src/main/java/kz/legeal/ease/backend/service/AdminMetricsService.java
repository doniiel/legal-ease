package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.SystemMetricsDto;

public interface AdminMetricsService {

    /** Collect aggregate system-wide counts for the admin dashboard. */
    SystemMetricsDto getMetrics();
}
