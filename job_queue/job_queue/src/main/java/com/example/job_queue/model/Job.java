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