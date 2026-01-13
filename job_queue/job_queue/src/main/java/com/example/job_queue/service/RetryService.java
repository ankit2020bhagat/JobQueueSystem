package com.jobqueue.service;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import com.jobqueue.repository.DeadLetterQueueRepository;
import com.jobqueue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * RetryService - Implements exponential backoff retry mechanism
 *
 * Key Features:
 * - Exponential backoff: delay = initialDelay * (multiplier ^ retryCount)
 * - Maximum retry attempts configurable
 * - Failed jobs moved to Dead Letter Queue after max retries
 * - Automatic retry scheduling
 *
 * Example: Initial delay = 1s, Multiplier = 2
 * Retry 1: 1s, Retry 2: 2s, Retry 3: 4s, Retry 4: 8s, Retry 5: 16s
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final JobRepository jobRepository;
    private final DeadLetterQueueRepository dlqRepository;
    private final KafkaTemplate<String, Job> kafkaTemplate;

    // Retry configuration
    private static final long INITIAL_DELAY_MS = 1000;  // 1 second
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_DELAY_MS = 60000;     // 60 seconds

    /**
     * Schedule retry for a failed job
     * Calculates delay using exponential backoff
     */
    public void scheduleRetry(Job job) {
        if (job.getRetryCount() >= job.getMaxRetries()) {
            moveToDeadLetterQueue(job);
            return;
        }

        long delay = calculateBackoffDelay(job.getRetryCount());

        log.info("Scheduling retry for job {}: attempt {}, delay {}ms",
                job.getId(), job.getRetryCount() + 1, delay);

        // In production, use scheduled executor or delay queue
        // For demo, we'll process in the next scheduled check
    }

    /**
     * Calculate exponential backoff delay
     * Formula: min(initialDelay * multiplier^retryCount, maxDelay)
     */
    private long calculateBackoffDelay(int retryCount) {
        long delay = (long) (INITIAL_DELAY_MS * Math.pow(MULTIPLIER, retryCount));
        return Math.min(delay, MAX_DELAY_MS);
    }

    /**
     * Periodic check for jobs eligible for retry
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processRetries() {
        List<Job> jobsToRetry = jobRepository.findJobsEligibleForRetry();

        if (!jobsToRetry.isEmpty()) {
            log.info("Found {} jobs eligible for retry", jobsToRetry.size());

            for (Job job : jobsToRetry) {
                long timeSinceLastAttempt = calculateTimeSinceLastAttempt(job);
                long requiredDelay = calculateBackoffDelay(job.getRetryCount() - 1);

                if (timeSinceLastAttempt >= requiredDelay) {
                    retryJob(job);
                }
            }
        }
    }

    /**
     * Retry a job by resubmitting to Kafka
     */
    private void retryJob(Job job) {
        log.info("Retrying job: id={}, attempt={}", job.getId(), job.getRetryCount() + 1);

        job.setStatus(JobStatus.PENDING);
        job.setErrorMessage(null);
        jobRepository.save(job);

        kafkaTemplate.send(KafkaConfig.JOB_TOPIC, job);
    }

    /**
     * Move job to Dead Letter Queue after max retries
     */
    @Transactional
    public void moveToDeadLetterQueue(Job job) {
        log.warn("Moving job {} to Dead Letter Queue after {} failed attempts",
                job.getId(), job.getRetryCount());

        job.setStatus(JobStatus.DEAD_LETTER);
        Job dlqJob = jobRepository.save(job);

        // Also save to DLQ repository for separate tracking
        dlqRepository.save(dlqJob);

        // Publish to DLQ topic for alerting
        kafkaTemplate.send(KafkaConfig.DLQ_TOPIC, dlqJob);
    }

    private long calculateTimeSinceLastAttempt(Job job) {
        if (job.getStartedAt() == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(
                job.getStartedAt(),
                java.time.LocalDateTime.now()
        ).toMillis();
    }
}