package com.eeum.posts.dto.response;

public record GetMyPostsResponse(
        String postId,
        String title,
        String artworkUrl,
        Boolean isCompleted
) {
}
