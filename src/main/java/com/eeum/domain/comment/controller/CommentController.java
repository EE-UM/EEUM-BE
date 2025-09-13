package com.eeum.domain.comment.controller;

import com.eeum.domain.comment.docs.CommentApi;
import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.service.CommentService;
import com.eeum.global.support.response.ApiResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ApiResponse<List<CommentResponse>> readAllCommentsOfPost(
            @PathVariable("postId") Long postId
    ) {
        List<CommentResponse> response = commentService.readAllCommentsOfPost(postId);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<CommentResponse> create(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.create(userPrincipal, request);
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
