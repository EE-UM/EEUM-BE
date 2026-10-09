package com.eeum.domain.notification.fcm.dto.response;

import com.eeum.domain.notification.fcm.entity.NotificationType;

public record NotificationSettingResponse(
    NotificationType type,
    boolean enabled
) {

    public static NotificationSettingResponse of(NotificationType type, boolean enabled) {
        return new NotificationSettingResponse(type, enabled);
    }
}
