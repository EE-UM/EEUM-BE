package com.eeum.domain.posts.dto.response;

import lombok.Builder;

public record CreatePostResponse(
        Long postId,
        Long userId
) {

    public static CreatePostResponse of(Long postId, Long userId) {
        return CreatePostResponse.builder()
                .postId(postId)
                .userId(userId)
                .build();
    }

    @Builder
    public CreatePostResponse(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
