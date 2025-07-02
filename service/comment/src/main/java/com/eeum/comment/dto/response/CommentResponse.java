package com.eeum.comment.dto.response;

import com.eeum.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        String commentId,
        String content,
        String postId,
        String userId,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(String.valueOf(comment.getId()), comment.getContent(), String.valueOf(comment.getPostId()),
                String.valueOf(comment.getUserId()), false, comment.getCreatedAt(), comment.getModifiredAt());
    }
}
