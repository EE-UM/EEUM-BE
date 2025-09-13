package com.eeum.domain.comment.docs;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "Comment API")
public interface CommentApi {

    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글에 달린 모든 댓글을 조회합니다.")
    @GetMapping("/{postId}")
    ApiResponse<List<CommentResponse>> readAllCommentsOfPost(
            @PathVariable("postId") Long postId
    );

    @Operation(summary = "댓글 작성", description = "현재 로그인한 사용자가 새로운 댓글을 작성합니다.")
    @PostMapping
    ApiResponse<CommentResponse> create(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CommentCreateRequest request
    );

    @Operation(summary = "댓글 삭제", description = "현재 로그인한 사용자가 특정 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    ApiResponse<String> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("commentId") Long commentId
    );
}
