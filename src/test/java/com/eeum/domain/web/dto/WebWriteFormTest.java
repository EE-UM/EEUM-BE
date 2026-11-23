package com.eeum.domain.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.entity.CompletionType;
import org.junit.jupiter.api.Test;

class WebWriteFormTest {

    private static final WebMusic MUSIC = new WebMusic("앨범", "곡", "가수", "https://a/1.jpg",
        "https://p/1.m4a");

    @Test
    void 자동_마감은_고를_수_있는_개수만_허용한다() {
        assertThat(form(CompletionType.AUTO_COMPLETION, 20L).isCommentCountLimitValid()).isTrue();
        assertThat(form(CompletionType.AUTO_COMPLETION, 7L).isCommentCountLimitValid()).isFalse();
        assertThat(form(CompletionType.AUTO_COMPLETION, null).isCommentCountLimitValid()).isFalse();
        assertThat(form(CompletionType.MANUAL_COMPLETION, null).isCommentCountLimitValid()).isTrue();
    }

    @Test
    void 수동_마감은_개수를_넘기지_않는다() {
        CreatePostRequest request = form(CompletionType.MANUAL_COMPLETION, 30L).toRequest();

        assertThat(request.commentCountLimit()).isNull();
        assertThat(request.title()).isEqualTo("제목");
        assertThat(request.appleMusicUrl()).isEqualTo("https://p/1.m4a");
    }

    @Test
    void 자동_마감은_고른_개수를_넘긴다() {
        assertThat(form(CompletionType.AUTO_COMPLETION, 50L).toRequest().commentCountLimit())
            .isEqualTo(50L);
    }

    private static WebWriteForm form(CompletionType type, Long limit) {
        return new WebWriteForm(" 제목 ", "본문", MUSIC, type, limit);
    }
}
