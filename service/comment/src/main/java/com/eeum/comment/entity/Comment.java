package com.eeum.comment.entity;

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
    private Long id;

    private String content;

    private Long postId;

    private Long userId;

    private String username;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime modifiredAt;


    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public static Comment of(Long id, String content, Long postId, Long userId, String username) {
        LocalDateTime now = LocalDateTime.now();
        return Comment.builder()
                .id(id)
                .content(content)
                .postId(postId)
                .userId(userId)
                .username(username)
                .isDeleted(false)
                .createdAt(now)
                .modifiredAt(now)
                .build();
    }

    @Builder
    public Comment(Long id, String content, Long postId, Long userId, String username, Boolean isDeleted, LocalDateTime createdAt, LocalDateTime modifiredAt) {
        this.id = id;
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.modifiredAt = modifiredAt;
    }
}
