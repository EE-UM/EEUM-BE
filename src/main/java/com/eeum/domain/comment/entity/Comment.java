package com.eeum.domain.comment.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Table(name = "comments")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
public class Comment {

    @Id
    @Tsid
    private Long id;

    private String content;

    private Long postId;

    private Long userId;

    private String username;

    @Embedded
    private Album album;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime modifiredAt;



    public void updateContent(String content) {
        this.content = content;
    }

    public static Comment of(String content, Long postId, Long userId, String username, Album album) {
        LocalDateTime now = LocalDateTime.now();
        return Comment.builder()
                .content(content)
                .postId(postId)
                .userId(userId)
                .username(username)
                .album(album)
                .isDeleted(false)
                .createdAt(now)
                .modifiredAt(now)
                .build();
    }

    @Builder
    public Comment(String content, Long postId, Long userId, String username, Album album, Boolean isDeleted, LocalDateTime createdAt, LocalDateTime modifiredAt) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.album = album;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.modifiredAt = modifiredAt;
    }
}
