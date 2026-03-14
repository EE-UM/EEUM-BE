package com.eeum.domain.posts.dto.request;

public record UpdatePostRequest(
        Long postId,
        String title,
        String content,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl
) {
}
