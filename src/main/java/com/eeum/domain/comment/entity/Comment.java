package com.eeum.domain.comment.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
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

    private String artworkUrl;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime modifiredAt;



    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public static Comment of(String content, Long postId, Long userId, String username, String artworkUrl) {
        LocalDateTime now = LocalDateTime.now();
        return Comment.builder()
                .content(content)
                .postId(postId)
                .userId(userId)
                .username(username)
                .artworkUrl(artworkUrl)
                .isDeleted(false)
                .createdAt(now)
                .modifiredAt(now)
                .build();
    }

    @Builder
    public Comment(String content, Long postId, Long userId, String username, String artworkUrl, Boolean isDeleted, LocalDateTime createdAt, LocalDateTime modifiredAt) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.artworkUrl = artworkUrl;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.modifiredAt = modifiredAt;
    }
}
