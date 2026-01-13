package com.jobqueue.service;

import com.jobqueue.model.JobMetrics;
import com.jobqueue.model.JobStatus;
import com.jobqueue.model.JobPriority;
import com.jobqueue.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * MetricsService - Collects and broadcasts job metrics
 *
 * Provides:
 * - Real-time job counts by status
 * - Success/failure rates
 * - Average processing times
 * - Queue depth by priority
 * - Hourly statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final JobRepository jobRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Calculate and broadcast metrics every 5 seconds
     */
    @Scheduled(fixedDelay = 5000)
    public void calculateAndBroadcastMetrics() {
        JobMetrics metrics = calculateMetrics();

        // Broadcast to WebSocket clients
        messagingTemplate.convertAndSend("/topic/metrics", metrics);

        log.debug("Metrics updated: pending={}, processing={}, completed={}",
                metrics.getPendingJobs(),
                metrics.getProcessingJobs(),
                metrics.getCompletedJobs());
    }

    /**
     * Calculate comprehensive job metrics
     */
    public JobMetrics calculateMetrics() {
        // Total counts by status
        Long totalJobs = jobRepository.count();
        Long pendingJobs = jobRepository.countByStatus(JobStatus.PENDING);
        Long processingJobs = jobRepository.countByStatus(JobStatus.PROCESSING);
        Long completedJobs = jobRepository.countByStatus(JobStatus.COMPLETED);
        Long failedJobs = jobRepository.countByStatus(JobStatus.FAILED);
        Long deadLetterJobs = jobRepository.countByStatus(JobStatus.DEAD_LETTER);

        // Queue depth by priority
        Long highPriorityPending = jobRepository.countByStatusAndPriority(
                JobStatus.PENDING, JobPriority.HIGH);
        Long mediumPriorityPending = jobRepository.countByStatusAndPriority(
                JobStatus.PENDING, JobPriority.MEDIUM);
        Long lowPriorityPending = jobRepository.countByStatusAndPriority(
                JobStatus.PENDING, JobPriority.LOW);

        // Success and failure rates
        Long totalProcessed = completedJobs + failedJobs + deadLetterJobs;
        Double successRate = totalProcessed > 0
                ? (completedJobs * 100.0) / totalProcessed
                : 0.0;
        Double failureRate = totalProcessed > 0
                ? ((failedJobs + deadLetterJobs) * 100.0) / totalProcessed
                : 0.0;

        // Average processing time
        Double avgProcessingTime = jobRepository.getAverageProcessingTime();
        if (avgProcessingTime == null) {
            avgProcessingTime = 0.0;
        }

        // Last hour statistics
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long jobsProcessedLastHour = jobRepository
                .findByStatusAndCreatedAtAfter(JobStatus.COMPLETED, oneHourAgo)
                .size();
        Long jobsFailedLastHour = jobRepository
                .findByStatusAndCreatedAtAfter(JobStatus.FAILED, oneHourAgo)
                .size();

        return JobMetrics.builder()
                .totalJobs(totalJobs)
                .pendingJobs(pendingJobs)
                .processingJobs(processingJobs)
                .completedJobs(completedJobs)
                .failedJobs(failedJobs)
                .deadLetterJobs(deadLetterJobs)
                .highPriorityPending(highPriorityPending)
                .mediumPriorityPending(mediumPriorityPending)
                .lowPriorityPending(lowPriorityPending)
                .successRate(successRate)
                .failureRate(failureRate)
                .averageProcessingTime(avgProcessingTime)
                .jobsProcessedLastHour(jobsProcessedLastHour)
                .jobsFailedLastHour(jobsFailedLastHour)
                .build();
    }
}