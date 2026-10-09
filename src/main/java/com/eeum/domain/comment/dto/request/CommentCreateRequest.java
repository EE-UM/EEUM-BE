package com.eeum.domain.comment.dto.request;


import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
    String content,
    @NotBlank
    String albumName,
    @NotBlank
    String songName,
    @NotBlank
    String artistName,
    @NotBlank
    String artworkUrl,
    @NotBlank
    String appleMusicUrl,
    @NotBlank
    Long postId
) {

}
