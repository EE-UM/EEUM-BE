package com.eeum.domain.notification.fcm.controller;

import com.eeum.domain.notification.fcm.docs.DeviceTokenApi;
import com.eeum.domain.notification.fcm.dto.request.DeviceTokenDeleteRequest;
import com.eeum.domain.notification.fcm.dto.request.DeviceTokenRegisterRequest;
import com.eeum.domain.notification.fcm.service.DeviceTokenService;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/device-tokens")
public class DeviceTokenController implements DeviceTokenApi {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ApiResponse<Object> register(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody @Valid DeviceTokenRegisterRequest request
    ) {
        deviceTokenService.register(userPrincipal.getId(), request);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Object> unregister(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody DeviceTokenDeleteRequest request
    ) {
        deviceTokenService.unregister(userPrincipal.getId(), request);
        return ApiResponse.success();
    }
}
