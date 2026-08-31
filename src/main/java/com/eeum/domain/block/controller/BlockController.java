package com.eeum.domain.block.controller;

import com.eeum.domain.block.docs.BlockApi;
import com.eeum.domain.block.dto.request.BlockUserRequest;
import com.eeum.domain.block.dto.request.UnblockUserRequest;
import com.eeum.domain.block.dto.response.BlockUserResponse;
import com.eeum.domain.block.dto.response.UnblockUserResponse;
import com.eeum.domain.block.service.BlockService;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class BlockController implements BlockApi {

    private final BlockService blockService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/block")
    public ApiResponse<BlockUserResponse> block(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody BlockUserRequest blockUserRequest) {
        BlockUserResponse blockUserResponse = blockService.block(userPrincipal.getId(),
            blockUserRequest);
        return ApiResponse.success(blockUserResponse);
    }

    @DeleteMapping("/unblock")
    public ApiResponse<UnblockUserResponse> unblock(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody UnblockUserRequest unblockUserRequest) {
        UnblockUserResponse unblockUserResponse = blockService.unblock(userPrincipal.getId(),
            unblockUserRequest);
        return ApiResponse.success(unblockUserResponse);
    }
}
