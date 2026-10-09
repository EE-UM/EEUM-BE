package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.notification.fcm.dto.request.DeviceTokenDeleteRequest;
import com.eeum.domain.notification.fcm.dto.request.DeviceTokenRegisterRequest;
import com.eeum.domain.notification.fcm.entity.DeviceToken;
import com.eeum.domain.notification.fcm.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void register(Long userId, DeviceTokenRegisterRequest request) {
        deviceTokenRepository.findByFcmToken(request.fcmToken())
            .ifPresentOrElse(
                token -> token.changeOwner(userId, request.platform()),
                () -> deviceTokenRepository.save(
                    DeviceToken.of(request.platform(), request.fcmToken(), userId))
            );
    }

    @Transactional
    public void unregister(Long userId, DeviceTokenDeleteRequest request) {
        deviceTokenRepository.findByFcmToken(request.fcmToken())
            .filter(token -> token.getUserId().equals(userId))
            .ifPresent(deviceTokenRepository::delete);
    }
}
