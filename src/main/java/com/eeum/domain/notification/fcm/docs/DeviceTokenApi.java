package com.eeum.domain.notification.fcm.docs;

import com.eeum.domain.notification.fcm.dto.request.DeviceTokenDeleteRequest;
import com.eeum.domain.notification.fcm.dto.request.DeviceTokenRegisterRequest;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "DeviceToken", description = "푸시 알림 디바이스 토큰 API")
public interface DeviceTokenApi {

    @Operation(summary = "디바이스 토큰 등록", description = "로그인 직후 또는 FCM 토큰이 갱신될 때 호출합니다. 이미 등록된 토큰이면 현재 사용자로 소유자를 변경합니다.")
    ApiResponse<Object> register(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody DeviceTokenRegisterRequest request
    );

    @Operation(summary = "디바이스 토큰 삭제", description = "로그아웃할 때 호출합니다.")
    ApiResponse<Object> unregister(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody DeviceTokenDeleteRequest request
    );
}
