package com.eeum.posts.dto.response;

import java.time.LocalDateTime;

public record GetPostByIdResponse(
        String postId,
        String title,
        String content,
        String userId,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        LocalDateTime createdAt


) {
}
