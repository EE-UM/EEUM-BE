package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findAllByUserId(Long userId);

    void deleteByFcmToken(String fcmToken);

    @Transactional
    void deleteAllByFcmTokenIn(List<String> fcmTokens);
}
