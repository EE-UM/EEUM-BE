package com.eeum.global.batch;

import java.time.LocalDateTime;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRecorder {

    private final BatchJobExecutionRepository batchJobExecutionRepository;

    public void run(String jobName, Supplier<BatchResult> job) {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            BatchResult result = job.get();
            if (result.isEmpty()) {
                return;
            }

            save(BatchJobExecution.succeeded(jobName, result, startedAt, LocalDateTime.now()));
        } catch (Exception e) {
            save(BatchJobExecution.failed(jobName, e, startedAt, LocalDateTime.now()));
            throw e;
        }
    }

    private void save(BatchJobExecution execution) {
        try {
            batchJobExecutionRepository.save(execution);
        } catch (Exception e) {
            log.error("[Batch] 실행 이력 저장 실패 - job: {}", execution.getJobName(), e);
        }
    }
}
