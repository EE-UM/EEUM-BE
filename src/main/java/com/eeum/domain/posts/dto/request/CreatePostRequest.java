package com.eeum.domain.posts.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotNull
        String title,
        String content,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl
) {
}
