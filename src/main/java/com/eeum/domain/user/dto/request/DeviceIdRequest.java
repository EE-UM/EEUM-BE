package com.eeum.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeviceIdRequest(

    @NotBlank(message = "디바이스 아이디는 공백일 수 없습니다.")
    @Schema(description = "디바이스 고유 값", example = "ADJQNS123J")
    String deviceId,
    @Schema(description = "로그인 타입", example = "guest")
    String provider
) {

}
