package com.eeum.like.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.like.dto.response.LikeResponse;
import com.eeum.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/posts/{postId}")
    public ApiResponse<LikeResponse> read(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(likeService.read(postId, userPrincipal.getId()));
    }

    @PostMapping("/posts/{postId}")
    public ApiResponse<String> like(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        likeService.like(postId, userPrincipal.getId());
        return ApiResponse.success("좋아요 추가 완료");
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<String> unlike(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        likeService.unlike(postId, userPrincipal.getId());
        return ApiResponse.success("좋아요 삭제 완료");
    }
}
