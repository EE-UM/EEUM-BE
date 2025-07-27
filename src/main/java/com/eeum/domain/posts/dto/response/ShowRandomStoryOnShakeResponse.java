package com.eeum.domain.posts.dto.response;

public record ShowRandomStoryOnShakeResponse(
        String postId,
        String writerId,
        String title,
        String content
) {
}
