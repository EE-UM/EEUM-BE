package com.eeum.domain.notification.fcm.dto.request;

import com.eeum.domain.notification.fcm.entity.Platform;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRegisterRequest(
    @NotNull
    Platform platform,
    @NotNull
    String fcmToken
) {

}
