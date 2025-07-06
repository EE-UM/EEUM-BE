package com.eeum.postsread.dto.response;

import com.eeum.postsread.client.CommentClient;
import com.eeum.postsread.client.PostsClient;
import lombok.Getter;

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
        List<CommentClient.CommentResponse> comments
) {
    public static PostsReadResponse of(PostsClient.PostsResponse post, List<CommentClient.CommentResponse> comments) {
        return new PostsReadResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getSongName(),
                post.getArtistName(),
                post.getArtworkUrl(),
                post.getAppleMusicUrl(),
                post.getCreatedAt(),
                comments
        );
    }
}
