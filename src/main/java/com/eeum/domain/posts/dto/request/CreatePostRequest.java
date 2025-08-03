package com.eeum.domain.posts.dto.request;

import com.eeum.domain.posts.entity.CompletionType;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotNull
        String title,
        String content,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        CompletionType completionType,
        Long commentCountLimit
) {
}
