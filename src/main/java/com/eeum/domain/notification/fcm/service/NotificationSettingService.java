package com.eeum.domain.notification.fcm.service;

import com.eeum.domain.notification.fcm.dto.request.NotificationSettingUpdateRequest;
import com.eeum.domain.notification.fcm.dto.response.NotificationSettingResponse;
import com.eeum.domain.notification.fcm.entity.NotificationSetting;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.repository.NotificationSettingRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    public List<NotificationSettingResponse> readAll(Long userId) {
        Map<NotificationType, NotificationSetting> settings = notificationSettingRepository.findAllByUserId(userId)
            .stream()
            .collect(Collectors.toMap(NotificationSetting::getType, Function.identity()));

        // 설정 row가 없는 유형은 기본값(켜짐)으로 응답
        return Arrays.stream(NotificationType.values())
            .map(type -> NotificationSettingResponse.of(
                type,
                settings.containsKey(type) ? settings.get(type).isEnabled() : true
            ))
            .toList();
    }

    @Transactional
    public NotificationSettingResponse update(Long userId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = notificationSettingRepository.findByUserIdAndType(userId, request.type())
            .orElseGet(() -> notificationSettingRepository.save(
                NotificationSetting.createDefault(userId, request.type())));
        setting.changeEnabled(request.enabled());
        return NotificationSettingResponse.of(setting.getType(), setting.isEnabled());
    }
}
