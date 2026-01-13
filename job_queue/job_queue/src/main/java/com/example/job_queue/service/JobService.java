package com.jobqueue.service;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobPriority;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import com.jobqueue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JobService - Main service for job management
 *
 * Responsibilities:
 * - Creating and submitting jobs
 * - Updating job status
 * - Managing job lifecycle
 * - Publishing events to Kafka
 * - Broadcasting updates via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final KafkaTemplate<String, Job> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Submit a new job to the queue
     *
     * @param jobType Type of job to execute
     * @param payload Job data in JSON format
     * @param priority Job priority level
     * @return Created job entity
     */
    @Transactional
    public Job submitJob(String jobType, String payload, JobPriority priority) {
        log.info("Submitting new job: type={}, priority={}", jobType, priority);

        Job job = Job.builder()
                .jobType(jobType)
                .payload(payload)
                .priority(priority)
                .status(JobStatus.PENDING)
                .retryCount(0)
                .maxRetries(5)
                .build();

        job = jobRepository.save(job);

        // Publish to Kafka for distributed processing
        kafkaTemplate.send(KafkaConfig.JOB_TOPIC, job);

        // Broadcast to WebSocket for real-time dashboard
        messagingTemplate.convertAndSend("/topic/jobs", job);

        log.info("Job submitted successfully: id={}", job.getId());
        return job;
    }

    /**
     * Schedule a job for future execution
     *
     * @param jobType Type of job
     * @param payload Job data
     * @param priority Priority level
     * @param scheduledTime When to execute
     * @return Created scheduled job
     */
    @Transactional
    public Job scheduleJob(String jobType, String payload, JobPriority priority,
                           LocalDateTime scheduledTime) {
        log.info("Scheduling job for: {}", scheduledTime);

        Job job = Job.builder()
                .jobType(jobType)
                .payload(payload)
                .priority(priority)
                .status(JobStatus.SCHEDULED)
                .scheduledTime(scheduledTime)
                .retryCount(0)
                .maxRetries(5)
                .build();

        return jobRepository.save(job);
    }

    /**
     * Create a recurring job with cron expression
     *
     * @param jobType Type of job
     * @param payload Job data
     * @param priority Priority level
     * @param cronExpression Cron schedule
     * @return Created recurring job template
     */
    @Transactional
    public Job createRecurringJob(String jobType, String payload, JobPriority priority,
                                  String cronExpression) {
        log.info("Creating recurring job: cron={}", cronExpression);

        Job job = Job.builder()
                .jobType(jobType)
                .payload(payload)
                .priority(priority)
                .status(JobStatus.SCHEDULED)
                .cronExpression(cronExpression)
                .retryCount(0)
                .maxRetries(5)
                .build();

        return jobRepository.save(job);
    }

    /**
     * Update job status
     *
     * @param jobId Job identifier
     * @param status New status
     */
    @Transactional
    public void updateJobStatus(Long jobId, JobStatus status) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setStatus(status);

            if (status == JobStatus.PROCESSING) {
                job.setStartedAt(LocalDateTime.now());
            } else if (status == JobStatus.COMPLETED) {
                job.setCompletedAt(LocalDateTime.now());
            }

            jobRepository.save(job);

            // Broadcast status update
            messagingTemplate.convertAndSend("/topic/job-status", job);
        }
    }

    /**
     * Mark job as failed
     *
     * @param jobId Job identifier
     * @param errorMessage Error details
     */
    @Transactional
    public void markJobFailed(Long jobId, String errorMessage) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setRetryCount(job.getRetryCount() + 1);

            jobRepository.save(job);

            log.warn("Job failed: id={}, retryCount={}, error={}",
                    jobId, job.getRetryCount(), errorMessage);
        }
    }

    /**
     * Get job by ID
     */
    public Optional<Job> getJob(Long jobId) {
        return jobRepository.findById(jobId);
    }
}