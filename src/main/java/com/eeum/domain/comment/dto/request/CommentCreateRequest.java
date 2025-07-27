package com.eeum.domain.comment.dto.request;


public record CommentCreateRequest(
        String content,
        String artworkUrl,
        Long postId
) {
}
