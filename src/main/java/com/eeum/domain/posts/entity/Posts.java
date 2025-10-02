package com.eeum.domain.posts.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_created_at", columnList = "created_at DESC")
        }
)
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posts {

    @Id
    @Tsid
    private Long id;

    private String title;

    private String content;

    @Embedded
    private Album album;

    private Long userId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isDeleted;

    private Boolean isCompleted;

    @Enumerated(EnumType.STRING)
    private CompletionType completionType;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIsCompleted() {
        this.isCompleted = Boolean.TRUE;
    }

    public void updateCompletionType(CompletionType completionType) {
        this.completionType = completionType;
    }

    public static Posts of(String title, String content, Album album, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return Posts.builder()
                .title(title)
                .content(content)
                .album(album)
                .userId(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Builder
    private Posts(String title, String content, Album album, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.album = album;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = false;
        this.isCompleted = false;
    }
}
