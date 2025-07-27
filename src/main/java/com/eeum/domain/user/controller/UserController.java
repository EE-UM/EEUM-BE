package com.eeum.domain.user.controller;

import com.eeum.global.support.response.ApiResponse;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
