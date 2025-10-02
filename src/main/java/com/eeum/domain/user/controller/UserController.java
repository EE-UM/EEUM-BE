package com.eeum.domain.user.controller;

import com.eeum.domain.user.docs.UserApi;
import com.eeum.domain.user.dto.request.DeviceIdRequest;
import com.eeum.domain.user.dto.response.GetProfileResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<GetProfileResponse> getProfile(@CurrentUser UserPrincipal userPrincipal) {
        GetProfileResponse profile = userService.getProfile(userPrincipal.getId());
        return ApiResponse.success(profile);
    }

    @PostMapping("/guest")
    public ApiResponse<LoginResponse> guestLogin(@RequestBody DeviceIdRequest deviceIdRequest) {
        LoginResponse loginResponse = userService.guestLogin(deviceIdRequest);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody IdTokenRequest idTokenRequest) {
        LoginResponse loginResponse = userService.login(idTokenRequest);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/test")
    public ApiResponse<LoginResponse> testLogin(@RequestBody IdTokenRequest idTokenRequest) {
        LoginResponse loginResponse = userService.testLogin();
        System.out.println("accessToken" + loginResponse.accessToken());
        return ApiResponse.success(loginResponse);
    }
}
