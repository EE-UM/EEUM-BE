package com.eeum.domain.comment.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@Table(name = "comments",
    indexes = {
        @Index(name = "idx_post_id_deleted_created_at", columnList = "postId, deleted, createdAt"),
        @Index(name = "idx_post_id_deleted_user_id", columnList = "postId, deleted, userId"),
        @Index(name = "idx_user_id_post_id", columnList = "userId, post_id")
    })
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(columnName = "deleted", strategy = SoftDeleteType.TIMESTAMP)
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

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public static Comment of(String content, Long postId, Long userId, String username,
        Album album) {
        LocalDateTime now = LocalDateTime.now();
        return Comment.builder()
            .content(content)
            .postId(postId)
            .userId(userId)
            .username(username)
            .album(album)
            .createdAt(now)
            .modifiedAt(now)
            .build();
    }

    @Builder
    public Comment(String content, Long postId, Long userId, String username, Album album,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.album = album;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
