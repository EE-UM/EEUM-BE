package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findAllByUserId(Long userId);

    void deleteByFcmToken(String fcmToken);

    @Transactional
    void deleteAllByFcmTokenIn(List<String> fcmTokens);

    @Query(value = """
        SELECT DISTINCT dt.user_id FROM device_token dt
        WHERE NOT EXISTS (
            SELECT 1 FROM notification_setting ns
            WHERE ns.user_id = dt.user_id
                    AND ns.type = 'ANNOUNCEMENT'
                    AND ns.enabled = false)
        """, nativeQuery = true)
    List<Long> findAnnouncementTargetUserIds();
}
