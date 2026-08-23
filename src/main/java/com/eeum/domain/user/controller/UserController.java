package com.eeum.domain.user.controller;

import com.eeum.domain.user.docs.UserApi;
import com.eeum.domain.user.dto.request.BlockUserRequest;
import com.eeum.domain.user.dto.request.DeviceIdRequest;
import com.eeum.domain.user.dto.request.IdTokenRequest;
import com.eeum.domain.user.dto.request.UnblockUserRequest;
import com.eeum.domain.user.dto.request.UpdateProfileRequest;
import com.eeum.domain.user.dto.response.BlockUserResponse;
import com.eeum.domain.user.dto.response.GetProfileResponse;
import com.eeum.domain.user.dto.response.LoginResponse;
import com.eeum.domain.user.dto.response.UnblockUserResponse;
import com.eeum.domain.user.dto.response.UpdateProfileResponse;
import com.eeum.domain.user.service.UserService;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/block")
    public ApiResponse<BlockUserResponse> block(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody BlockUserRequest blockUserRequest) {
        BlockUserResponse blockUserResponse = userService.block(userPrincipal.getId(),
            blockUserRequest);
        return ApiResponse.success(blockUserResponse);
    }

    @DeleteMapping("/unblock")
    public ApiResponse<UnblockUserResponse> unblock(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody UnblockUserRequest unblockUserRequest) {
        UnblockUserResponse unblockUserResponse = userService.unblock(userPrincipal.getId(),
            unblockUserRequest);
        return ApiResponse.success(unblockUserResponse);
    }

    @DeleteMapping("/close")
    public ApiResponse<Void> closeAccount(
        @CurrentUser UserPrincipal userPrincipal
    ) {
        userService.closeAccount(userPrincipal.getId());

        return ApiResponse.success(null);
    }

    @PatchMapping("/profile")
    public ApiResponse<UpdateProfileResponse> updateProfile(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody UpdateProfileRequest updateProfileRequest) {
        UpdateProfileResponse updateProfileResponse = userService.updateProfile(
            userPrincipal.getId(), updateProfileRequest);
        return ApiResponse.success(updateProfileResponse);
    }

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
