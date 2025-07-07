package com.eeum.postsread.repository;

import com.eeum.postsread.client.PostsClient;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostsQueryModel {
    private Long postId;
    private String title;
    private String content;
    private Long userId;
    private String songName;
    private String artistName;
    private String artworkUrl;
    private String appleMusicUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
//    private Long postCommentCount;
//    private Long postLikeCount;


    public static PostsQueryModel create(PostsClient.PostsResponse post) {
        PostsQueryModel postsQueryModel = new PostsQueryModel();
        postsQueryModel.postId = Long.valueOf(post.getPostId());
        postsQueryModel.title = post.getTitle();
        postsQueryModel.content = post.getContent();
        postsQueryModel.userId = Long.valueOf(post.getUserId());
        postsQueryModel.songName = post.getSongName();
        postsQueryModel.artistName = post.getArtistName();
        postsQueryModel.artworkUrl = post.getArtworkUrl();
        postsQueryModel.appleMusicUrl = post.getAppleMusicUrl();
        postsQueryModel.createdAt = post.getCreatedAt();
        postsQueryModel.modifiedAt = post.getModifiedAt();
//        postsQueryModel.postCommentCount = commentCount;
//        postsQueryModel.postLikeCount = postLikeCount;
        return postsQueryModel;
    }
}
