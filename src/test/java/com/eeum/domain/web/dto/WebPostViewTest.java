package com.eeum.domain.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.posts.dto.response.PostsReadResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebPostViewTest {

    @Test
    void 작성자_식별_정보는_담지_않고_아트워크는_600으로_요청한다() {
        CommentResponse comment = new CommentResponse(10L, "좋은 노래", 1L, 7L, "kakao_7", "댓글러",
            "앨범", "댓글 곡", "댓글 가수", "https://a/300x300bb.jpg", "https://p/c.m4a", false,
            LocalDateTime.now(), LocalDateTime.now());
        PostsReadResponse post = new PostsReadResponse(3L, 1L, "제목", "본문", "작성자", "곡", "가수",
            "https://is1.mzstatic.com/image/thumb/x/300x300bb.jpg", "https://p/post.m4a",
            LocalDateTime.now(), false, List.of(comment));

        WebPostView view = WebPostView.from(post);

        assertThat(view.artworkUrl()).isEqualTo(
            "https://is1.mzstatic.com/image/thumb/x/600x600bb.jpg");
        assertThat(view.previewUrl()).isEqualTo("https://p/post.m4a");
        assertThat(view.comments()).containsExactly(new WebPostView.WebCommentView(
            "댓글 곡", "댓글 가수", "https://a/300x300bb.jpg", "https://p/c.m4a", "좋은 노래"));
    }

    @Test
    void 댓글이_null이면_빈_목록이다() {
        PostsReadResponse post = new PostsReadResponse(3L, 1L, "제목", "본문", null, "곡", "가수",
            null, null, LocalDateTime.now(), false, null);

        WebPostView view = WebPostView.from(post);

        assertThat(view.comments()).isEmpty();
        assertThat(view.artworkUrl()).isNull();
    }

    @Test
    void 설명은_공백을_합치고_100자에서_자른다() {
        String content = "첫 줄\n\n둘째   줄 " + "가".repeat(200);

        String description = WebPostView.describe(content, "곡", "가수");

        assertThat(description).startsWith("첫 줄 둘째 줄 가");
        assertThat(description).endsWith("…");
        assertThat(description).hasSize(WebPostView.DESCRIPTION_MAX_LENGTH + 1);
    }

    @Test
    void 본문이_비어_있으면_곡_정보로_설명한다() {
        assertThat(WebPostView.describe("  \n ", "곡", "가수")).isEqualTo("곡 - 가수");
        assertThat(WebPostView.describe(null, "곡", null)).isEqualTo("곡");
        assertThat(WebPostView.describe("짧은 글", "곡", "가수")).isEqualTo("짧은 글");
    }
}
