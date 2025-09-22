package com.eeum.domain.comment.dto.response;

import com.eeum.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long postId,
        Long userId,
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
        return new CommentResponse(comment.getId(), comment.getContent(), comment.getPostId(),
                comment.getUserId(), comment.getUsername(),
                comment.getAlbum().getAlbumName(), comment.getAlbum().getSongName(),
                comment.getAlbum().getArtistName(), comment.getAlbum().getArtworkUrl(),comment.getAlbum().getAppleMusicUrl(),
                false, comment.getCreatedAt(), comment.getModifiredAt());
    }
}
