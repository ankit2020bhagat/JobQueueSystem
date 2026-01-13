package com.jobqueue.model;

/**
 * Job Status States
 *
 * PENDING: Job created, waiting to be processed
 * SCHEDULED: Job scheduled for future execution
 * PROCESSING: Currently being processed by a worker
 * COMPLETED: Successfully completed
 * FAILED: Failed but can be retried
 * DEAD_LETTER: Failed after max retries, moved to DLQ
 * CANCELLED: Job was cancelled
 */
public enum JobStatus {
    PENDING,
    SCHEDULED,
    PROCESSING,
    COMPLETED,
    FAILED,
    DEAD_LETTER,
    CANCELLED
}