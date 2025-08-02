package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.entity.Posts;

import java.time.LocalDateTime;

public record GetCommentedPostsResponse(
        Long postId,
        String artworkUrl,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetCommentedPostsResponse from(Posts posts) {
        return new GetCommentedPostsResponse(posts.getId(), posts.getAlbum().getArtworkUrl(), posts.getTitle(),
                posts.getCreatedAt(), posts.getUpdatedAt());
    }
}
