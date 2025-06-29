package com.eeum.posts.dto.response;

public record ShowRandomStoryOnShakeResponse(
        String postId,
        String writerId,
        String title,
        String content
) {
}
