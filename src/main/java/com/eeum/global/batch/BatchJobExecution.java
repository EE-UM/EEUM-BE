package com.eeum.global.batch;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "batch_job_execution")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJobExecution {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;

    @Tsid
    @Id
    private Long id;

    private String jobName;

    @Enumerated(EnumType.STRING)
    private BatchJobStatus status;

    private int processedCount;

    private int failedCount;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private BatchJobExecution(String jobName, BatchJobStatus status, int processedCount,
        int failedCount,
        String errorMessage, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.jobName = jobName;
        this.status = status;
        this.processedCount = processedCount;
        this.failedCount = failedCount;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static BatchJobExecution succeeded(String jobName, BatchResult result,
        LocalDateTime startedAt, LocalDateTime finishedAt) {
        return new BatchJobExecution(jobName, BatchJobStatus.SUCCEEDED, result.processedCount(),
            result.failedCount(), null, startedAt, finishedAt);
    }

    public static BatchJobExecution failed(String jobName, Exception e, LocalDateTime startedAt,
        LocalDateTime finishedAt) {
        return new BatchJobExecution(jobName, BatchJobStatus.FAILED, 0, 0,
            truncate(String.valueOf(e.getMessage())), startedAt, finishedAt);
    }

    private static String truncate(String message) {
        return message.length() > ERROR_MESSAGE_MAX_LENGTH
            ? message.substring(0, ERROR_MESSAGE_MAX_LENGTH)
            : message;
    }
}
