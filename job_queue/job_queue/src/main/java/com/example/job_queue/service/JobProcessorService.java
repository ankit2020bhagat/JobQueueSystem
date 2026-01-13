package com.jobqueue.service;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessorService {

    private final JobService jobService;
    private final RetryService retryService;

    @KafkaListener(topics = KafkaConfig.JOB_TOPIC, groupId = "job-queue-group")
    public void processJob(Job job) {
        log.info("Received job from queue: id={}, type={}", job.getId(), job.getJobType());

        try {
            jobService.updateJobStatus(job.getId(), JobStatus.PROCESSING);
            executeJob(job);
            jobService.updateJobStatus(job.getId(), JobStatus.COMPLETED);
            log.info("Job completed successfully: id={}", job.getId());

        } catch (Exception e) {
            log.error("Job execution failed: id={}, error={}", job.getId(), e.getMessage());
            jobService.markJobFailed(job.getId(), e.getMessage());
            retryService.scheduleRetry(job);
        }
    }

    private void executeJob(Job job) throws Exception {
        log.info("Executing job: id={}, type={}", job.getId(), job.getJobType());

        switch (job.getJobType()) {
            case "EMAIL":
                executeEmailJob(job);
                break;
            case "DATA_PROCESSING":
                executeDataProcessingJob(job);
                break;
            case "REPORT_GENERATION":
                executeReportGenerationJob(job);
                break;
            case "IMAGE_PROCESSING":
                executeImageProcessingJob(job);
                break;
            default:
                throw new IllegalArgumentException("Unknown job type: " + job.getJobType());
        }
    }

    private void executeEmailJob(Job job) throws Exception {
        log.info("Sending email for job: {}", job.getId());
        Thread.sleep(2000); // Simulate email sending
        log.info("Email sent successfully");
    }

    private void executeDataProcessingJob(Job job) throws Exception {
        log.info("Processing data for job: {}", job.getId());
        Thread.sleep(5000); // Simulate data processing
        log.info("Data processing completed");
    }

    private void executeReportGenerationJob(Job job) throws Exception {
        log.info("Generating report for job: {}", job.getId());
        Thread.sleep(8000); // Simulate report generation
        log.info("Report generated successfully");
    }

    private void executeImageProcessingJob(Job job) throws Exception {
        log.info("Processing image for job: {}", job.getId());
        Thread.sleep(3000); // Simulate image processing
        log.info("Image processed successfully");
    }
}