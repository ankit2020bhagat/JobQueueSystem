package com.jobqueue.service;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JobSchedulerService - Handles scheduled and recurring jobs
 *
 * Responsibilities:
 * - Checking for scheduled jobs ready to run
 * - Processing cron-based recurring jobs
 * - Moving scheduled jobs to pending state
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobSchedulerService {

    private final JobRepository jobRepository;
    private final JobService jobService;

    /**
     * Check for scheduled jobs every 10 seconds
     * Moves jobs from SCHEDULED to PENDING when their time arrives
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<Job> readyJobs = jobRepository.findScheduledJobsReadyToRun(now);

        if (!readyJobs.isEmpty()) {
            log.info("Found {} scheduled jobs ready to run", readyJobs.size());

            for (Job job : readyJobs) {
                // For recurring jobs, create a new instance
                if (job.getCronExpression() != null) {
                    createNextRecurringInstance(job);
                }

                // Move to pending for processing
                jobService.updateJobStatus(job.getId(), JobStatus.PENDING);
            }
        }
    }

    /**
     * Create next instance of a recurring job
     */
    private void createNextRecurringInstance(Job template) {
        // Calculate next execution time based on cron expression
        // For simplicity, using fixed intervals here
        // In production, use a cron parser library like Quartz

        LocalDateTime nextRun = calculateNextRunTime(template.getCronExpression());

        jobService.scheduleJob(
                template.getJobType(),
                template.getPayload(),
                template.getPriority(),
                nextRun
        );

        log.info("Created next instance of recurring job: type={}, nextRun={}",
                template.getJobType(), nextRun);
    }

    /**
     * Simple cron parser (for demo purposes)
     * In production, use org.quartz.CronExpression
     */
    private LocalDateTime calculateNextRunTime(String cronExpression) {
        // Parse cron: "0 0 * * *" = every day at midnight
        // For demo, just add 1 hour
        return LocalDateTime.now().plusHours(1);
    }
}
