package com.eeum.domain.notification.fcm.batch;

import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import com.eeum.domain.notification.fcm.service.NotificationDispatcher;
import com.eeum.global.batch.BatchJobRecorder;
import com.eeum.global.batch.BatchResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementDispatchJob {

    private static final int BATCH_SIZE = 500;

    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final ThreadPoolTaskExecutor announcementSendExecutor;

    private final BatchJobRecorder batchJobRecorder;

    @Scheduled(fixedDelay = 10_000, initialDelay = 60_000)
    public void dispatch() {
        batchJobRecorder.run("ANNOUNCEMENT_DISPATCH", this::dispatchPending);
    }

    private BatchResult dispatchPending() {
        long startedAt = System.currentTimeMillis();
        int total = 0;
        AtomicInteger failed = new AtomicInteger();

        List<Long> ids;
        while (!(ids = notificationRepository.findPendingAnnouncementIds(BATCH_SIZE)).isEmpty()) {
            dispatchAll(ids, failed);
            total += ids.size();
        }

        if (total > 0) {
            log.info("[Announcement] 공지 알림 {}건 처리 - {}ms", total,
                System.currentTimeMillis() - startedAt);
        }

        return BatchResult.of(total - failed.get(), failed.get());
    }

    private void dispatchAll(List<Long> ids, AtomicInteger failed) {
        CompletableFuture[] futures = ids.stream()
            .map(id -> CompletableFuture.runAsync(() -> dispatchSafely(id, failed),
                announcementSendExecutor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void dispatchSafely(Long id, AtomicInteger failed) {
        try {
            notificationDispatcher.dispatch(id);
        } catch (Exception e) {
            failed.incrementAndGet();
            log.warn("공지 알림 발송 실패 - notificationId: {}", id, e);
        }
    }
}
