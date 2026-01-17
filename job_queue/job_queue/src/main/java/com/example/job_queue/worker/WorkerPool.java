package com.jobqueue.worker;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.repository.JobRepository;
import com.jobqueue.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * WorkerPool - Manages parallel job processing
 *
 * Features:
 * - Polls database for pending jobs
 * - Distributes work across thread pool
 * - Respects priority ordering
 * - Processes multiple jobs concurrently
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerPool {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final JobWorker jobWorker;

    /**
     * Poll for pending jobs every 2 seconds
     * Submits jobs to worker threads for processing
     */
    @Scheduled(fixedDelay = 2000)
    public void pollAndProcessJobs() {
        List<Job> pendingJobs = jobRepository.findJobsToProcess(JobStatus.PENDING);

        if (!pendingJobs.isEmpty()) {
            log.info("Found {} pending jobs to process", pendingJobs.size());

            // Process jobs asynchronously
            for (Job job : pendingJobs) {
                processJobAsync(job);
            }
        }
    }

    /**
     * Process job asynchronously using thread pool
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> processJobAsync(Job job) {
        return CompletableFuture.runAsync(() -> {
            jobWorker.executeJob(job);
        });
    }
}