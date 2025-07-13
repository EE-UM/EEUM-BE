package com.eeum.like.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "like_count")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeCount {

    @Id
    private Long postId;
    private Long likeCount;

    public void increase() {
        this.likeCount += 1;
    }

    public void decrease() {
        this.likeCount -= 1;
    }

    public static LikeCount of(Long postId, Long likeCount) {
        return LikeCount.builder()
                .postId(postId)
                .likeCount(likeCount)
                .build();
    }

    @Builder
    private LikeCount(Long postId, Long likeCount) {
        this.postId = postId;
        this.likeCount = likeCount;
    }
}
