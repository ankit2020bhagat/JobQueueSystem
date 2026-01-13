





package com.jobqueue.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobMetrics - Real-time metrics for monitoring
 *
 * Tracks:
 * - Total jobs processed
 * - Success/failure rates
 * - Average processing time
 * - Queue sizes by priority
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMetrics {
    private Long totalJobs;
    private Long pendingJobs;
    private Long processingJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Long deadLetterJobs;

    private Long highPriorityPending;
    private Long mediumPriorityPending;
    private Long lowPriorityPending;

    private Double successRate;
    private Double failureRate;
    private Double averageProcessingTime;

    private Long jobsProcessedLastHour;
    private Long jobsFailedLastHour;
}