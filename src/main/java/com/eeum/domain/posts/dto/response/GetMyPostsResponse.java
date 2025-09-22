package com.eeum.domain.posts.dto.response;

public record GetMyPostsResponse(
        Long postId,
        String title,
        String artworkUrl,
        Boolean isCompleted
) {
}
