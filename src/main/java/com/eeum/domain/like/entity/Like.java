package com.eeum.domain.like.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
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
    @Tsid
    private Long id;

    private Long userId;

    private Long postId;

    private LocalDateTime createdAt;


    public static Like of(Long userId, Long postId) {
        LocalDateTime now = LocalDateTime.now();
        Like like = Like.builder()
                .userId(userId)
                .postId(postId)
                .createdAt(now)
                .build();

        return like;
    }

    @Builder
    private Like(Long userId, Long postId, LocalDateTime createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }
}
