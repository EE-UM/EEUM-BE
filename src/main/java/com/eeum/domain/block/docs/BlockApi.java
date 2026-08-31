package com.eeum.domain.block.docs;

import com.eeum.domain.block.dto.request.BlockUserRequest;
import com.eeum.domain.block.dto.request.UnblockUserRequest;
import com.eeum.domain.block.dto.response.BlockUserResponse;
import com.eeum.domain.block.dto.response.UnblockUserResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Block", description = "Block API")
public interface BlockApi {

    @Operation(summary = "유저 차단", description = "특정 유저를 차단합니다.")
    @PostMapping("/block")
    ApiResponse<BlockUserResponse> block(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody BlockUserRequest blockUserRequest);

    @Operation(summary = "유저 차단 해제", description = "특정 유저의 차단을 해제합니다.")
    @DeleteMapping("/unblock")
    ApiResponse<UnblockUserResponse> unblock(@CurrentUser UserPrincipal userPrincipal,
        @RequestBody UnblockUserRequest unblockUserRequest);
}
