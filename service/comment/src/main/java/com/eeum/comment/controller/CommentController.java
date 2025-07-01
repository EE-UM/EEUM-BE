package com.eeum.comment.controller;

import com.eeum.comment.dto.request.CommentCreateRequest;
import com.eeum.comment.dto.response.CommentResponse;
import com.eeum.comment.service.CommentService;
import com.eeum.common.aop.auth.RequireLogin;
import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @RequireLogin
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> read(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("commentId") Long commentId
    ) {
        CommentResponse response = commentService.read(commentId);
        return ApiResponse.success(response);
    }

    @RequireLogin
    @PostMapping
    public ApiResponse<CommentResponse> create(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(userPrincipal.getId(), request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<String> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("commentId") Long commentId
            ) {
        commentService.delete(userPrincipal.getId(), commentId);
        return ApiResponse.success("댓글을 삭제했습니다.");
    }
}
