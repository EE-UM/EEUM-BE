package com.eeum.domain.user.docs;

import com.eeum.domain.user.dto.request.DeviceIdRequest;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "User", description = "User API")
public interface UserApi {

    @Operation(summary = "게스트 로그인", description = "Device의 고유 ID값으로 게스트 로그인합니다.")
    @PostMapping("/guest")
    ApiResponse<LoginResponse> guestLogin(@RequestBody DeviceIdRequest deviceIdRequest);

    @Operation(summary = "로그인", description = "Apple/Kakao 등 외부 인증에서 발급받은 IdToken을 이용해 로그인합니다.")
    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@RequestBody IdTokenRequest idTokenRequest);

    @Operation(summary = "테스트 로그인", description = "테스트 용도로 고정된 토큰을 반환합니다.")
    @PostMapping("/test")
    ApiResponse<LoginResponse> testLogin(@RequestBody IdTokenRequest idTokenRequest);
}
