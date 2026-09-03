package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.repository.PostsQueryModel;

import java.time.LocalDateTime;

public record PostsReadInfiniteScrollResponse(
    Long postId,
    Long userId,
    String title,
    String content,
    String nickname,
    String songName,
    String artistName,
    String artworkUrl,
    String appleMusicUrl,
    LocalDateTime createdAt,
    boolean isCompleted
) {

    public static PostsReadInfiniteScrollResponse from(PostsQueryModel postsQueryModel) {
        return new PostsReadInfiniteScrollResponse(
            postsQueryModel.getPostId(),
            postsQueryModel.getUserId(),
            postsQueryModel.getTitle(),
            postsQueryModel.getContent(),
            postsQueryModel.getNickname(),
            postsQueryModel.getSongName(),
            postsQueryModel.getArtistName(),
            postsQueryModel.getArtworkUrl(),
            postsQueryModel.getAppleMusicUrl(),
            postsQueryModel.getCreatedAt(),
            postsQueryModel.isCompleted()
        );
    }
}
