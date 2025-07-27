package com.eeum.domain.comment.dto.response;

import com.eeum.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        String commentId,
        String content,
        String postId,
        String userId,
        String username,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(String.valueOf(comment.getId()), comment.getContent(), String.valueOf(comment.getPostId()),
                String.valueOf(comment.getUserId()), comment.getUsername(), false, comment.getCreatedAt(), comment.getModifiredAt());
    }
}
