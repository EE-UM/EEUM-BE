package com.eeum.domain.notification.fcm.batch;

import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import com.eeum.domain.notification.fcm.service.NotificationDispatcher;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatchRetryJob {

    private static final Duration PENDING_GRACE = Duration.ofMinutes(1);
    private static final int BATCH_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void retry() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> ids = notificationRepository.findDispatchTargetIds(
            now.minus(PENDING_GRACE), now, BATCH_SIZE);
        if (ids.isEmpty()) {
            return;
        }

        log.info("[Batch] 알림 재발송 대상 {}건", ids.size());
        for (Long id : ids) {
            try {
                notificationDispatcher.dispatch(id);
            } catch (Exception e) {
                log.error("알림 재발송 실패 - notificationId: {}", id, e);
            }
        }
    }
}
