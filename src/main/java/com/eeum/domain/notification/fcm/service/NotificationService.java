package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
import com.eeum.domain.notification.fcm.entity.DeviceToken;
import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationSetting;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.entity.TargetType;
import com.eeum.domain.notification.fcm.port.PushSender;
import com.eeum.domain.notification.fcm.port.dto.response.PushResult;
import com.eeum.domain.notification.fcm.repository.DeviceTokenRepository;
import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import com.eeum.domain.notification.fcm.repository.NotificationSettingRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    @Transactional
    public void sendFirstComment(FirstCommentCreatedEvent event) {
        NotificationType type = NotificationType.FIRST_COMMENT;
        Long receiverId = event.postAuthorId();

        if (notificationRepository.existsByTypeAndTargetTypeAndTargetId(
            type, TargetType.POST, event.postId()
        )) {
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

        List<String> tokens = deviceTokenRepository.findAllByUserId(receiverId).stream()
            .map(DeviceToken::getFcmToken)
            .toList();
        if (tokens.isEmpty()) {
            notification.markFailed();
            return;
        }

        Map<String, String> data = Map.of(
            "type", type.name(),
            "postId", String.valueOf(event.postId())
        );
        PushResult result = pushSender.send(tokens, title, body, data);

        if (result.isAllFailed()) {
            notification.markFailed();
        } else {
            notification.markSent();
        }
        if (!result.invalidTokens().isEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(result.invalidTokens());
        }
    }

    private boolean isEnabled(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type)
            .map(NotificationSetting::isEnabled)
            .orElse(true);
    }
}
