package com.jobqueue.worker;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.service.JobService;
import com.jobqueue.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * JobWorker - Individual worker that executes jobs
 *
 * Each worker:
 * - Claims a job
 * - Executes the job logic
 * - Updates status
 * - Handles failures
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobWorker {

    private final JobService jobService;
    private final RetryService retryService;

    private final String workerId = UUID.randomUUID().toString();

    /**
     * Execute a job
     */
    public void executeJob(Job job) {
        try {
            log.info("Worker {} starting job {}", workerId, job.getId());

            // Update status to processing
            jobService.updateJobStatus(job.getId(), JobStatus.PROCESSING);

            // Simulate job execution
            performJobWork(job);

            // Mark as completed
            jobService.updateJobStatus(job.getId(), JobStatus.COMPLETED);

            log.info("Worker {} completed job {}", workerId, job.getId());

        } catch (Exception e) {
            log.error("Worker {} failed job {}: {}",
                    workerId, job.getId(), e.getMessage());

            jobService.markJobFailed(job.getId(), e.getMessage());
            retryService.scheduleRetry(job);
        }
    }

    /**
     * Perform actual job work based on job type
     */
    private void performJobWork(Job job) throws Exception {
        switch (job.getJobType()) {
            case "EMAIL":
                log.info("Sending email...");
                Thread.sleep(2000);
                break;
            case "DATA_PROCESSING":
                log.info("Processing data...");
                Thread.sleep(5000);
                break;
            case "REPORT_GENERATION":
                log.info("Generating report...");
                Thread.sleep(8000);
                break;
            default:
                log.info("Executing generic job...");
                Thread.sleep(3000);
        }
    }
}
