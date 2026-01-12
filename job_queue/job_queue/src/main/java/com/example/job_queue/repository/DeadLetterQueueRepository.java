package com.jobqueue.repository;

import com.jobqueue.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DeadLetterQueueRepository - Stores jobs that failed after max retries
 *
 * These jobs require manual intervention or investigation
 */
@Repository
public interface DeadLetterQueueRepository extends JpaRepository<Job, Long> {
    // Inherits standard CRUD operations from JpaRepository
}