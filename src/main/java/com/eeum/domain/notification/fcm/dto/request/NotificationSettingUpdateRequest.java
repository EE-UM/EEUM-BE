package com.eeum.domain.notification.fcm.dto.request;

import com.eeum.domain.notification.fcm.entity.NotificationType;
import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateRequest(
    @NotNull
    NotificationType type,
    @NotNull
    Boolean enabled
) {

}
