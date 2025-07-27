package com.eeum.domain.posts.dto.response;

public record GetMyPostsResponse(
        String postId,
        String title,
        String artworkUrl,
        Boolean isCompleted
) {
}
