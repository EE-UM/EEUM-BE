package com.eeum.domain.like.controller;

import com.eeum.global.support.response.ApiResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.domain.like.dto.response.LikeResponse;
import com.eeum.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/users/{userId}")
    public ApiResponse<List<LikeResponse>> readUserLikedPosts(@PathVariable("userId") Long userId) {
        return ApiResponse.success(likeService.readUserLikedPosts(userId));
    }

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

    @GetMapping("/count/{postId}")
    public ApiResponse<Long> count(
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(likeService.count(postId));
    }
}
