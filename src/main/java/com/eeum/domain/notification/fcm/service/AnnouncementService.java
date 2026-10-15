package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.repository.DeviceTokenRepository;
import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public int create(String title, String body) {
        List<Notification> rows = deviceTokenRepository.findAnnouncementTargetUserIds().stream()
            .map(userId -> Notification.of(userId, NotificationType.ANNOUNCEMENT, title, body, null,
                null))
            .toList();
        notificationRepository.saveAll(rows);

        log.info("[Announcement] 공지 알림 {}건 생성", rows.size());
        return rows.size();
    }
}
