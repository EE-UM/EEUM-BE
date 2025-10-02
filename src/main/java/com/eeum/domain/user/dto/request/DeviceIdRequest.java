package com.eeum.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceIdRequest(
        @Schema(description = "디바이스 고유 값", example = "ADJQNS123J")
        String deviceId,
        @Schema(description = "로그인 타입", example = "guest")
        String provider
) {
}
