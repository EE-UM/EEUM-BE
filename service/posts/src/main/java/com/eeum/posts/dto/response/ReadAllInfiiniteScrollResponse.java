package com.eeum.posts.dto.response;

import com.eeum.posts.entity.Posts;

import java.time.LocalDateTime;

public record ReadAllInfiiniteScrollResponse(
        String postId,
        String userId,
        String albumArtworkUrl,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReadAllInfiiniteScrollResponse from(Posts posts) {
        return new ReadAllInfiiniteScrollResponse(
                String.valueOf(posts.getId()),
                String.valueOf(posts.getUserId()),
                posts.getAlbum().getArtworkUrl(),
                posts.getTitle(),
                posts.getCreatedAt(),
                posts.getUpdatedAt()
        );
    }
}
