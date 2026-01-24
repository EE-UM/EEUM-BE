package com.eeum.domain.posts.dto.response;

public record GetMyPostResponse(
        Long postId,
        String title,
        String artworkUrl,
        Boolean isCompleted,
        Long currentPlaylistCount,
        Long targetPlaylistCount
) {
}
