package com.eeum.posts.dto.request;

public record CreatePostRequest(
        String title,
        String content,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl
) {
}
