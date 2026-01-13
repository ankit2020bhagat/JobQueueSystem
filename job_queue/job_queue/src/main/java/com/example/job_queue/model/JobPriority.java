package com.jobqueue.model;

/**
 * Job Priority Levels
 *
 * HIGH: Critical jobs that need immediate processing
 * MEDIUM: Normal priority jobs
 * LOW: Background jobs that can wait
 */
public enum JobPriority {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int value;

    JobPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}