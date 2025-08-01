package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.entity.Posts;
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
    private LocalDateTime updatedAt;


    public static PostsQueryModel create(Posts posts) {
        PostsQueryModel postsQueryModel = new PostsQueryModel();
        postsQueryModel.postId = posts.getId();
        postsQueryModel.title = posts.getTitle();
        postsQueryModel.content = posts.getContent();
        postsQueryModel.userId = posts.getUserId();
        postsQueryModel.songName = posts.getAlbum().getSongName();
        postsQueryModel.artistName = posts.getAlbum().getArtistName();
        postsQueryModel.artworkUrl = posts.getAlbum().getArtworkUrl();
        postsQueryModel.appleMusicUrl = posts.getAlbum().getAppleMusicUrl();
        postsQueryModel.createdAt = posts.getCreatedAt();
        postsQueryModel.updatedAt = posts.getUpdatedAt();
        return postsQueryModel;
    }

    public static PostsQueryModel of(
            Long postId,
            String title,
            String content,
            Long userId,
            String songName,
            String artistName,
            String artworkUrl,
            String appleMusicUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        PostsQueryModel postsQueryModel = new PostsQueryModel();
        postsQueryModel.postId = postId;
        postsQueryModel.title = title;
        postsQueryModel.content = content;
        postsQueryModel.userId = userId;
        postsQueryModel.songName = songName;
        postsQueryModel.artistName = artistName;
        postsQueryModel.artworkUrl = artworkUrl;
        postsQueryModel.appleMusicUrl = appleMusicUrl;
        postsQueryModel.createdAt = createdAt;
        postsQueryModel.updatedAt = updatedAt;
        return postsQueryModel;
    }
}
