package com.eeum.domain.posts.dto.response;

public record CompletePostResponse(
        Long postId,
        Long userId,
        Boolean isCompleted
) {
    public static CompletePostResponse of(Long postId, Long userId, Boolean isCompleted) {
        return new CompletePostResponse(postId, userId, isCompleted);
    }
}
