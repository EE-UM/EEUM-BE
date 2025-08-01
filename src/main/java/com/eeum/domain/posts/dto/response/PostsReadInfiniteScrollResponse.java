package com.eeum.domain.posts.dto.response;

import com.eeum.domain.posts.repository.PostsQueryModel;

import java.time.LocalDateTime;

public record PostsReadInfiniteScrollResponse(
        String postId,
        String title,
        String content,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        LocalDateTime createdAt
) {

    public static PostsReadInfiniteScrollResponse from(PostsQueryModel postsQueryModel) {
        return new PostsReadInfiniteScrollResponse(
                String.valueOf(postsQueryModel.getPostId()),
                postsQueryModel.getTitle(),
                postsQueryModel.getContent(),
                postsQueryModel.getSongName(),
                postsQueryModel.getArtistName(),
                postsQueryModel.getArtworkUrl(),
                postsQueryModel.getAppleMusicUrl(),
                postsQueryModel.getCreatedAt()
        );
    }
}
