package com.eeum.like.dto.response;

import com.eeum.like.entity.Like;

import java.time.LocalDateTime;

public record LikeResponse(
        Long id,

        Long userId,

        Long postId,

        LocalDateTime createdAt
) {
    public static LikeResponse from(Like like) {
        return new LikeResponse(
                like.getId(),
                like.getUserId(),
                like.getPostId(),
                like.getCreatedAt()
        );
    }
}
