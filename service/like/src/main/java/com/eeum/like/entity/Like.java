package com.eeum.like.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "likes",
indexes = {
        @Index(name = "idx_post_id_user_id", columnList = "postId, userId", unique = true)
})
@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

    @Id
    private Long id;

    private Long userId;

    private Long postId;

    private LocalDateTime createdAt;


    public static Like of(Long id, Long userId, Long postId) {
        LocalDateTime now = LocalDateTime.now();
        Like like = Like.builder()
                .id(id)
                .userId(userId)
                .postId(postId)
                .createdAt(now)
                .build();

        return like;
    }

    @Builder
    private Like(Long id, Long userId, Long postId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }
}
