package com.eeum.comment.dto.request;


public record CommentCreateRequest(
        String content,
        Long postId
) {
}
