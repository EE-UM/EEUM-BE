package com.eeum.domain.web.dto;

import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.entity.CompletionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 웹 사연 작성 폼 (앱 ShareScreen + CompletionSheet 와 같은 범위).
 */
public record WebWriteForm(
    @NotBlank(message = "사연의 제목을 작성해 주세요.")
    @Size(max = TITLE_MAX_LENGTH, message = "제목은 50자까지 쓸 수 있어요.")
    String title,
    @Size(max = CONTENT_MAX_LENGTH, message = "사연은 200자까지 쓸 수 있어요.")
    String content,
    @NotNull(message = "노래를 골라 주세요.") @Valid WebMusic music,
    @NotNull(message = "마감 방식을 골라 주세요.") CompletionType completionType,
    Long commentCountLimit
) {

    public static final int TITLE_MAX_LENGTH = 50;
    public static final int CONTENT_MAX_LENGTH = 200;
    public static final List<Long> LIMIT_OPTIONS = List.of(10L, 20L, 30L, 50L, 100L);
    public static final long DEFAULT_LIMIT = 20L;

    public static WebWriteForm empty() {
        return new WebWriteForm("", "", WebMusic.empty(), CompletionType.AUTO_COMPLETION,
            DEFAULT_LIMIT);
    }

    @AssertTrue(message = "최대 댓글 개수를 골라 주세요.")
    public boolean isCommentCountLimitValid() {
        return completionType != CompletionType.AUTO_COMPLETION
            || (commentCountLimit != null && LIMIT_OPTIONS.contains(commentCountLimit));
    }

    /**
     * 수동 마감은 개수를 넘기지 않는다 (서버 기본 100).
     */
    public CreatePostRequest toRequest() {
        Long limit = completionType == CompletionType.AUTO_COMPLETION ? commentCountLimit : null;
        return new CreatePostRequest(title.strip(), content == null ? "" : content.strip(),
            music.albumName(), music.songName(), music.artistName(), music.artworkUrl(),
            music.appleMusicUrl(), completionType, limit);
    }
}
