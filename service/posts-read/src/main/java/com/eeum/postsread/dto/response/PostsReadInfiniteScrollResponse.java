package com.eeum.postsread.dto.response;

import com.eeum.postsread.client.CommentClient;
import com.eeum.postsread.repository.PostsQueryModel;

import java.time.LocalDateTime;
import java.util.List;

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
