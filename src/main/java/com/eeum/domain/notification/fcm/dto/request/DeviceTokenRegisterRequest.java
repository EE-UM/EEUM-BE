package com.eeum.domain.notification.fcm.dto.request;

import com.eeum.domain.notification.fcm.entity.Platform;
import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRegisterRequest(
    @NotBlank
    Platform platform,
    @NotBlank
    String fcmToken
) {

}
