package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
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
        NotificationType type = NotificationType.FIRST_COMMENT;
        Long receiverId = event.postAuthorId();

        if (notificationRepository.existsByTypeAndTargetTypeAndTargetId(type, TargetType.POST,
            event.postId())) {
            return;
        }
        if (!isEnabled(receiverId, type)) {
            return;
        }

        String title = "첫 댓글이 달렸어요";
        String body = "'" + event.postTitle() + "'에 첫 번째 음악 댓글이 도착했어요.";
        Notification notification = notificationRepository.save(
            Notification.of(receiverId, type, title, body, TargetType.POST, event.postId())
        );

        eventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId()));
    }

    private boolean isEnabled(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type)
            .map(NotificationSetting::isEnabled)
            .orElse(true);
    }
}
