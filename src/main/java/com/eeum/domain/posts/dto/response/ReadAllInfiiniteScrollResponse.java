package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.entity.Posts;

import java.time.LocalDateTime;

public record ReadAllInfiiniteScrollResponse(
        Long postId,
        Long userId,
        String albumArtworkUrl,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReadAllInfiiniteScrollResponse from(Posts posts) {
        return new ReadAllInfiiniteScrollResponse(
                posts.getId(),
                posts.getUserId(),
                posts.getAlbum().getArtworkUrl(),
                posts.getTitle(),
                posts.getCreatedAt(),
                posts.getUpdatedAt()
        );
    }
}
