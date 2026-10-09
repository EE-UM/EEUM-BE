package com.eeum.domain.notification.fcm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenDeleteRequest(
    @NotBlank
    String fcmToken
) {

}
