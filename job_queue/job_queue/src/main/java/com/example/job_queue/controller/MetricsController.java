package com.jobqueue.controller;

import com.jobqueue.model.JobMetrics;
import com.jobqueue.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MetricsController - REST API for job metrics
 *
 * Endpoints:
 * GET /api/metrics - Get current job metrics
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Get current job metrics
     *
     * Returns statistics including:
     * - Job counts by status
     * - Success/failure rates
     * - Queue depth
     * - Processing times
     */
    @GetMapping
    public ResponseEntity<JobMetrics> getMetrics() {
        JobMetrics metrics = metricsService.calculateMetrics();
        return ResponseEntity.ok(metrics);
    }
}