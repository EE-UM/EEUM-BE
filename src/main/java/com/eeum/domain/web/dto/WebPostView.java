package com.eeum.domain.web.dto;

import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.posts.dto.response.PostsReadResponse;
import java.util.List;

/**
 * 공개 웹 페이지용 게시글 뷰. 비로그인 외부 노출이므로 작성자 id·닉네임 등 사용자 식별 정보는 담지 않는다.
 */
public record WebPostView(
    Long postId,
    String title,
    String content,
    String songName,
    String artistName,
    String artworkUrl,
    String previewUrl,
    String description,
    boolean completed,
    List<WebCommentView> comments
) {

    static final int DESCRIPTION_MAX_LENGTH = 100;

    public static WebPostView from(PostsReadResponse post) {
        return from(post, false);
    }

    public static WebPostView from(PostsReadResponse post, boolean completed) {
        List<WebCommentView> comments = post.comments() == null ? List.of()
            : post.comments().stream().map(WebCommentView::from).toList();
        return new WebPostView(
            post.postId(),
            post.title(),
            post.content(),
            post.songName(),
            post.artistName(),
            upscaleArtwork(post.artworkUrl()),
            post.appleMusicUrl(),
            describe(post.content(), post.songName(), post.artistName()),
            completed,
            comments
        );
    }

    /**
     * Apple Music 검색 시 300x300 으로 저장한 아트워크를 히어로 영역(최대 432px)에 맞게 600x600 으로 요청한다.
     */
    static String upscaleArtwork(String artworkUrl) {
        return artworkUrl == null ? null : artworkUrl.replace("300x300bb", "600x600bb");
    }

    /**
     * 링크 미리보기(og:description)용 한 줄 요약. 본문이 비어 있으면 곡 정보로 대신한다.
     */
    static String describe(String content, String songName, String artistName) {
        String normalized = content == null ? "" : content.replaceAll("\\s+", " ").strip();
        if (normalized.isEmpty()) {
            return songName == null ? "" : songName + (artistName == null ? "" : " - " + artistName);
        }
        if (normalized.length() <= DESCRIPTION_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, DESCRIPTION_MAX_LENGTH).strip() + "…";
    }

    public record WebCommentView(
        String songName,
        String artistName,
        String artworkUrl,
        String previewUrl,
        String content
    ) {

        static WebCommentView from(CommentResponse comment) {
            return new WebCommentView(comment.songName(), comment.artistName(),
                comment.artworkUrl(), comment.appleMusicUrl(), comment.content());
        }
    }
}
