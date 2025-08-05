package com.eeum.domain.comment.dto.request;


public record CommentCreateRequest(
        String content,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        Long postId
) {
}
