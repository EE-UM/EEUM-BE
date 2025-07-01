package com.eeum.comment.dto.request;

import java.time.LocalDateTime;

public record CommentCreateRequest(
        String content,
        Long postId
) {
}
