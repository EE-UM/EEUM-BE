package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.comment.event.CommentLimitReachedEvent;
import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
import com.eeum.domain.comment.event.HalfCommentReachedEvent;
import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationSetting;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.entity.TargetType;
import com.eeum.domain.notification.fcm.event.NotificationCreatedEvent;
import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import com.eeum.domain.notification.fcm.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createFirstComment(FirstCommentCreatedEvent event) {
        createPostNotification(NotificationType.FIRST_COMMENT, event.postAuthorId(), event.postId(),
            "첫 댓글이 달렸어요",
            "'" + event.postTitle() + "'에 첫 번째 음악 댓글이 도착했어요.");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createHalfComment(HalfCommentReachedEvent event) {
        createPostNotification(NotificationType.HALF_COMMENT, event.postAuthorId(), event.postId(),
            "목표의 절반을 넘었어요",
            "'" + event.postTitle() + "'에 음악 댓글이 벌써 절반이나 모였어요.");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createPostCompleted(CommentLimitReachedEvent event) {
        createPostNotification(NotificationType.POST_COMPLETED, event.postAuthorId(), event.postId(),
            "목표를 달성했어요",
            "'" + event.postTitle() + "'에 목표한 음악 댓글이 모두 모였어요.");
    }

    /**
     * 게시글 대상 알림 행을 PENDING 으로 저장한다. 게시글마다 유형별로 한 번만 저장하고, 수신자가 끈 유형은 건너뛴다.
     */
    private void createPostNotification(NotificationType type, Long receiverId, Long postId,
        String title, String body) {
        if (notificationRepository.existsByTypeAndTargetTypeAndTargetId(type, TargetType.POST,
            postId)) {
            return;
        }
        if (!isEnabled(receiverId, type)) {
            return;
        }

        Notification notification = notificationRepository.save(
            Notification.of(receiverId, type, title, body, TargetType.POST, postId)
        );

        eventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId()));
    }

    private boolean isEnabled(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type)
            .map(NotificationSetting::isEnabled)
            .orElse(true);
    }
}
