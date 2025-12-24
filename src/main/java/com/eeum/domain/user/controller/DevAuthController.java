package com.eeum.domain.user.controller;

import com.eeum.domain.user.docs.DevAuthApi;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.service.UserService;
import com.eeum.global.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class DevAuthController implements DevAuthApi {

    private final UserService userService;

    @PostMapping("/dev-master")
    public ApiResponse<LoginResponse> devGuestMasterLogin() {
        LoginResponse loginResponse = userService.devGuestMasterLogin();
        return ApiResponse.success(loginResponse);
    }
}
