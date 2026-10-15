package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.notification.fcm.entity.DeviceToken;
import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.TargetType;
import com.eeum.domain.notification.fcm.port.PushSender;
import com.eeum.domain.notification.fcm.port.dto.response.PushResult;
import com.eeum.domain.notification.fcm.repository.DeviceTokenRepository;
import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final Duration LEASE = Duration.ofSeconds(30);

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    public void dispatch(Long notificationId) {
        LocalDateTime now = LocalDateTime.now();
        if (notificationRepository.claim(notificationId, now, now.plus(LEASE)) == 0) {
            return;
        }

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow();
        List<String> tokens = deviceTokenRepository.findAllByUserId(notification.getUserId())
            .stream()
            .map(DeviceToken::getFcmToken)
            .toList();
        if (tokens.isEmpty()) {
            notificationRepository.markFailed(notificationId);
            return;
        }

        PushResult result;
        try {
            result = pushSender.send(tokens, notification.getTitle(), notification.getBody(),
                buildData(notification));
        } catch (RuntimeException e) {
            log.warn("알림 전송 실패 - notificationId: {}", notificationId, e);
            notificationRepository.markFailed(notificationId);
            return;
        }

        if (result.isAllFailed()) {
            notificationRepository.markFailed(notificationId);
        } else {
            notificationRepository.markSent(notificationId, LocalDateTime.now());
        }
        if (!result.invalidTokens().isEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(result.invalidTokens());
        }
    }

    private Map<String, String> buildData(Notification notification) {
        HashMap<String, String> data = new HashMap<>();
        data.put("type", notification.getType().name());
        if (notification.getTargetType() == TargetType.POST) {
            data.put("postId", String.valueOf(notification.getTargetId()));
        }

        return data;
    }
}
