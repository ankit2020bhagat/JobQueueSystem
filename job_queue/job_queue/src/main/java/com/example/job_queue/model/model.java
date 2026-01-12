package com.jobqueue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Job Entity - Represents a job in the queue system
 *
 * Key Fields:
 * - id: Unique identifier
 * - jobType: Type of job (EMAIL, DATA_PROCESSING, REPORT_GENERATION, etc.)
 * - priority: Job priority (HIGH, MEDIUM, LOW)
 * - status: Current status (PENDING, PROCESSING, COMPLETED, FAILED, DEAD_LETTER)
 * - payload: JSON payload containing job data
 * - retryCount: Number of retry attempts
 * - maxRetries: Maximum allowed retries
 * - scheduledTime: When job should execute (for scheduled jobs)
 * - cronExpression: Cron expression for recurring jobs
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_status_priority", columnList = "status, priority, createdAt"),
        @Index(name = "idx_scheduled_time", columnList = "scheduledTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private Integer maxRetries = 5;

    private LocalDateTime scheduledTime;

    private String cronExpression;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String workerId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = JobStatus.PENDING;
        }
    }
}

package com.jobqueue.model;

/**
 * Job Priority Levels
 *
 * HIGH: Critical jobs that need immediate processing
 * MEDIUM: Normal priority jobs
 * LOW: Background jobs that can wait
 */
public enum JobPriority {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int value;

    JobPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

package com.jobqueue.model;

/**
 * Job Status States
 *
 * PENDING: Job created, waiting to be processed
 * SCHEDULED: Job scheduled for future execution
 * PROCESSING: Currently being processed by a worker
 * COMPLETED: Successfully completed
 * FAILED: Failed but can be retried
 * DEAD_LETTER: Failed after max retries, moved to DLQ
 * CANCELLED: Job was cancelled
 */
public enum JobStatus {
    PENDING,
    SCHEDULED,
    PROCESSING,
    COMPLETED,
    FAILED,
    DEAD_LETTER,
    CANCELLED
}

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