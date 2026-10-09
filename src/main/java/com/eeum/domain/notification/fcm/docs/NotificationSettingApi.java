package com.eeum.domain.notification.fcm.docs;

import com.eeum.domain.notification.fcm.dto.request.NotificationSettingUpdateRequest;
import com.eeum.domain.notification.fcm.dto.response.NotificationSettingResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "NotificationSetting", description = "알림 유형별 수신 설정 API")
public interface NotificationSettingApi {

    @Operation(summary = "알림 설정 조회", description = "모든 알림 유형의 수신 여부를 반환합니다. 변경한 적 없는 유형은 기본값(true)으로 내려갑니다.")
    ApiResponse<List<NotificationSettingResponse>> readAll(
        @CurrentUser UserPrincipal userPrincipal
    );

    @Operation(summary = "알림 설정 변경", description = "특정 알림 유형의 수신 여부를 변경합니다.")
    ApiResponse<NotificationSettingResponse> update(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody NotificationSettingUpdateRequest request
    );
}
