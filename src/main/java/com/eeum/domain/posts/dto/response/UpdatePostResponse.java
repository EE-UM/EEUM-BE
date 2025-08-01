package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.entity.Posts;

public record UpdatePostResponse(
        Long postId,
        Long userId
) {
    public static UpdatePostResponse from(Posts posts) {
        return new UpdatePostResponse(posts.getId(), posts.getUserId());
    }
}
