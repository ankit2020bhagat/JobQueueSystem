package com.jobqueue.controller;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobPriority;
import com.jobqueue.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JobController - REST API for job management
 *
 * Endpoints:
 * POST /api/jobs - Submit a new job
 * POST /api/jobs/schedule - Schedule a job
 * POST /api/jobs/recurring - Create recurring job
 * GET /api/jobs/{id} - Get job details
 * DELETE /api/jobs/{id} - Cancel a job
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    /**
     * Submit a new job
     *
     * Example request:
     * POST /api/jobs
     * {
     *   "jobType": "EMAIL",
     *   "payload": "{\"to\":\"user@example.com\",\"subject\":\"Hello\"}",
     *   "priority": "HIGH"
     * }
     */
    @PostMapping
    public ResponseEntity<Job> submitJob(@RequestBody JobSubmitRequest request) {
        log.info("Received job submission: type={}, priority={}",
                request.getJobType(), request.getPriority());

        Job job = jobService.submitJob(
                request.getJobType(),
                request.getPayload(),
                request.getPriority()
        );

        return ResponseEntity.ok(job);
    }

    /**
     * Schedule a job for future execution
     *
     * Example:
     * POST /api/jobs/schedule
     * {
     *   "jobType": "REPORT_GENERATION",
     *   "payload": "{\"reportId\":123}",
     *   "priority": "MEDIUM",
     *   "scheduledTime": "2026-01-14T10:00:00"
     * }
     */
    @PostMapping("/schedule")
    public ResponseEntity<Job> scheduleJob(@RequestBody JobScheduleRequest request) {
        log.info("Scheduling job for: {}", request.getScheduledTime());

        Job job = jobService.scheduleJob(
                request.getJobType(),
                request.getPayload(),
                request.getPriority(),
                request.getScheduledTime()
        );

        return ResponseEntity.ok(job);
    }

    /**
     * Create a recurring job with cron expression
     *
     * Example:
     * POST /api/jobs/recurring
     * {
     *   "jobType": "DATA_PROCESSING",
     *   "payload": "{\"datasetId\":456}",
     *   "priority": "LOW",
     *   "cronExpression": "0 0 * * *"
     * }
     */
    @PostMapping("/recurring")
    public ResponseEntity<Job> createRecurringJob(
            @RequestBody JobRecurringRequest request) {
        log.info("Creating recurring job: cron={}", request.getCronExpression());

        Job job = jobService.createRecurringJob(
                request.getJobType(),
                request.getPayload(),
                request.getPriority(),
                request.getCronExpression()
        );

        return ResponseEntity.ok(job);
    }

    /**
     * Get job details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return jobService.getJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cancel a job
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelJob(@PathVariable Long id) {
        jobService.updateJobStatus(id, com.jobqueue.model.JobStatus.CANCELLED);
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    @lombok.Data
    public static class JobSubmitRequest {
        private String jobType;
        private String payload;
        private JobPriority priority;
    }

    @lombok.Data
    public static class JobScheduleRequest {
        private String jobType;
        private String payload;
        private JobPriority priority;
        private LocalDateTime scheduledTime;
    }

    @lombok.Data
    public static class JobRecurringRequest {
        private String jobType;
        private String payload;
        private JobPriority priority;
        private String cronExpression;
    }
}