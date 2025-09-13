package com.eeum.domain.like.docs;

import com.eeum.domain.like.dto.response.LikeResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "Like", description = "Like API")
public interface LikeApi {

    @Operation(summary = "특정 사용자가 좋아요한 게시글 조회", description = "특정 사용자가 좋아요한 게시글 목록을 조회합니다.")
    @GetMapping("/users/{userId}")
    ApiResponse<List<LikeResponse>> readUserLikedPosts(@PathVariable("userId") Long userId);

    @Operation(summary = "게시글 좋아요 여부 조회", description = "현재 로그인한 사용자가 특정 게시글에 좋아요를 눌렀는지 조회합니다.")
    @GetMapping("/posts/{postId}")
    ApiResponse<LikeResponse> read(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "좋아요 추가", description = "현재 로그인한 사용자가 특정 게시글에 좋아요를 추가합니다.")
    @PostMapping("/posts/{postId}")
    ApiResponse<String> like(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "좋아요 취소", description = "현재 로그인한 사용자가 특정 게시글의 좋아요를 취소합니다.")
    @DeleteMapping("/posts/{postId}")
    ApiResponse<String> unlike(
            @Parameter(hidden = true) @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "좋아요 개수 조회", description = "특정 게시글의 좋아요 개수를 조회합니다.")
    @GetMapping("/count/{postId}")
    ApiResponse<Long> count(@PathVariable("postId") Long postId);
}
