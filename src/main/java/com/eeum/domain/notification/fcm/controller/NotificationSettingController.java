package com.eeum.domain.notification.fcm.controller;

import com.eeum.domain.notification.fcm.docs.NotificationSettingApi;
import com.eeum.domain.notification.fcm.dto.request.NotificationSettingUpdateRequest;
import com.eeum.domain.notification.fcm.dto.response.NotificationSettingResponse;
import com.eeum.domain.notification.fcm.service.NotificationSettingService;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification-settings")
public class NotificationSettingController implements NotificationSettingApi {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    public ApiResponse<List<NotificationSettingResponse>> readAll(
        @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(notificationSettingService.readAll(userPrincipal.getId()));
    }

    @PatchMapping
    public ApiResponse<NotificationSettingResponse> update(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody @Valid NotificationSettingUpdateRequest request
    ) {
        return ApiResponse.success(notificationSettingService.update(userPrincipal.getId(), request));
    }
}
