package com.eeum.user.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.user.dto.request.IdTokenRequest;
import com.eeum.user.dto.response.LoginResponse;
import com.eeum.user.service.UserService;
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
