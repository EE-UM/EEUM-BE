package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByTypeAndTargetTypeAndTargetId(NotificationType type, TargetType targetType,
        Long targetId);
}
