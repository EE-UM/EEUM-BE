package com.eeum.domain.comment.dto.request;


import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
    String content,
    @NotNull
    String albumName,
    @NotNull
    String songName,
    @NotNull
    String artistName,
    @NotNull
    String artworkUrl,
    @NotNull
    String appleMusicUrl,
    @NotNull
    Long postId
) {

}
