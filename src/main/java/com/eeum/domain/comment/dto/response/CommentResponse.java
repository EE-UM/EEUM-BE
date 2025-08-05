package com.eeum.domain.comment.dto.response;

import com.eeum.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        String commentId,
        String content,
        String postId,
        String userId,
        String username,
        String albumName,
        String songName,
        String artistName,
        String artworkUrl,
        String appleMusicUrl,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(String.valueOf(comment.getId()), comment.getContent(), String.valueOf(comment.getPostId()),
                String.valueOf(comment.getUserId()), comment.getUsername(),
                comment.getAlbum().getAlbumName(), comment.getAlbum().getSongName(),
                comment.getAlbum().getArtistName(), comment.getAlbum().getArtworkUrl(),comment.getAlbum().getAppleMusicUrl(),
                false, comment.getCreatedAt(), comment.getModifiredAt());
    }
}
