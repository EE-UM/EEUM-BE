package com.eeum.domain.comment.controller;

import com.eeum.domain.comment.docs.CommentApi;
import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.service.CommentService;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ApiResponse<List<CommentResponse>> readAllCommentsOfPost(
        @CurrentUser UserPrincipal userPrincipal,
        @PathVariable("postId") Long postId
    ) {
        Long currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        List<CommentResponse> response = commentService.readAllCommentsOfPost(postId,
            currentUserId);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<CommentResponse> create(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody @Valid CommentCreateRequest request
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
