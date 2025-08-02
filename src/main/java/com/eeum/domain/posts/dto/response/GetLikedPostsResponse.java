package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.entity.Posts;

import java.time.LocalDateTime;

public record GetLikedPostsResponse(
        Long postId,
        String artworkUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetLikedPostsResponse from(Posts posts) {
        return new GetLikedPostsResponse(posts.getId(), posts.getAlbum().getArtworkUrl(),
                posts.getCreatedAt(), posts.getUpdatedAt());
    }
}
