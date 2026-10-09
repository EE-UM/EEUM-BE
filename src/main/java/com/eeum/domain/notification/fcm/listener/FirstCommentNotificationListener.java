package com.eeum.domain.notification.fcm.listener;

import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
import com.eeum.domain.notification.fcm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirstCommentNotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FirstCommentCreatedEvent event) {
        try {
            notificationService.sendFirstComment(event);
        } catch (Exception e) {
            log.error("첫 댓글 알림 처리 실패 - postId: {}", event.postId(), e);
        }
    }
}
