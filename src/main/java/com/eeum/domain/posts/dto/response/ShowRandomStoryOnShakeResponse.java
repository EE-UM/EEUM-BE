package com.eeum.domain.posts.dto.response;

public record ShowRandomStoryOnShakeResponse(
        Long postId,
        Long writerId,
        String title,
        String content
) {
}
