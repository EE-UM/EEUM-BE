package com.eeum.global.batch;

public record BatchResult(
    int processedCount,
    int failedCount
) {

    public static final BatchResult EMPTY = new BatchResult(0, 0);

    public static BatchResult of(int processedCount, int failedCount) {
        return new BatchResult(processedCount, failedCount);
    }

    public boolean isEmpty() {
        return processedCount == 0 && failedCount == 0;
    }
}
