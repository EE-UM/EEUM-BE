package com.eeum.domain.web.dto;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WebCommentForm(
    @Size(max = WebCommentForm.CONTENT_MAX_LENGTH, message = "글은 200자까지 쓸 수 있어요.")
    String content,
    @NotNull(message = "노래를 골라 주세요.") @Valid WebMusic music
) {

    public static final int CONTENT_MAX_LENGTH = 200;

    public CommentCreateRequest toRequest(Long postId) {
        String trimmed = content == null ? null : content.strip();
        return new CommentCreateRequest(trimmed == null || trimmed.isEmpty() ? null : trimmed,
            music.albumName(), music.songName(), music.artistName(), music.artworkUrl(),
            music.appleMusicUrl(), postId);
    }
}
