package com.eeum.domain.user.docs;

import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "DevAuth", description = "Dev Master User API")
public interface DevAuthApi {

    @Operation(summary = "dev 서버 전용 마스터 계정 guest 로그인")
    @PostMapping("/guest/dev-master")
    ApiResponse<LoginResponse> devGuestMasterLogin();
}
