package com.eeum.domain.posts.dto.response;

import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.posts.repository.PostsQueryModel;

import java.time.LocalDateTime;
import java.util.List;

public record PostsReadResponse(
        String postId,
        String title,
        String content,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        LocalDateTime createdAt,
        Boolean isLiked,
        List<CommentResponse> comments
) {

    public static PostsReadResponse from(PostsQueryModel postsQueryModel, List<CommentResponse> comments) {
        return new PostsReadResponse(
                String.valueOf(postsQueryModel.getPostId()),
                postsQueryModel.getTitle(),
                postsQueryModel.getContent(),
                postsQueryModel.getSongName(),
                postsQueryModel.getArtistName(),
                postsQueryModel.getArtworkUrl(),
                postsQueryModel.getAppleMusicUrl(),
                postsQueryModel.getCreatedAt(),
                postsQueryModel.isLiked(),
                comments
        );
    }
}
