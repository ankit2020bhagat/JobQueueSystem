package com.jobqueue.repository;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;
import com.jobqueue.model.JobPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JobRepository - Data access layer for Job entities
 *
 * Provides methods for:
 * - Fetching jobs by status and priority
 * - Finding scheduled jobs
 * - Retrieving jobs for retry
 * - Statistics and metrics queries
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find next jobs to process, ordered by priority and creation time
    @Query("SELECT j FROM Job j WHERE j.status = :status " +
            "ORDER BY j.priority ASC, j.createdAt ASC")
    List<Job> findJobsToProcess(@Param("status") JobStatus status);

    // Find scheduled jobs ready to execute
    @Query("SELECT j FROM Job j WHERE j.status = 'SCHEDULED' " +
            "AND j.scheduledTime <= :now")
    List<Job> findScheduledJobsReadyToRun(@Param("now") LocalDateTime now);

    // Find jobs eligible for retry
    @Query("SELECT j FROM Job j WHERE j.status = 'FAILED' " +
            "AND j.retryCount < j.maxRetries")
    List<Job> findJobsEligibleForRetry();

    // Count jobs by status
    Long countByStatus(JobStatus status);

    // Count jobs by status and priority
    Long countByStatusAndPriority(JobStatus status, JobPriority priority);

    // Find jobs created in time range
    List<Job> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Find jobs by status and created after
    List<Job> findByStatusAndCreatedAtAfter(JobStatus status, LocalDateTime after);

    // Get average processing time
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, j.startedAt, j.completedAt)) " +
            "FROM Job j WHERE j.status = 'COMPLETED' " +
            "AND j.startedAt IS NOT NULL AND j.completedAt IS NOT NULL")
    Double getAverageProcessingTime();

    // Find jobs with cron expressions (recurring jobs)
    List<Job> findByCronExpressionIsNotNull();
}