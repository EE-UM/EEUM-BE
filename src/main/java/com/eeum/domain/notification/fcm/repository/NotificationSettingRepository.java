package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.NotificationSetting;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUserIdAndType(Long userId, NotificationType type);
}
