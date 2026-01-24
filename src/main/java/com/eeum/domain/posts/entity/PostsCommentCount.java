package com.eeum.domain.posts.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostsCommentCount {

    public static final long DEFAULT_CURRENT_COMMENT_COUNT = 0L;
    public static final int CURRENT_COMMENT_COUNT_INCREASE_UNIT = 1;

    @Id
    private Long postId;
    private Long targetCommentCount;
    private Long currentCommentCount;

    public static PostsCommentCount of(Long postId, Long targetCommentCount) {
        return PostsCommentCount.builder()
                .postId(postId)
                .targetCommentCount(targetCommentCount)
                .build();
    }

    @Builder
    public PostsCommentCount(Long postId, Long targetCommentCount) {
        this.postId = postId;
        this.targetCommentCount = targetCommentCount;
        this.currentCommentCount = DEFAULT_CURRENT_COMMENT_COUNT;
    }

    private void increaseCurrentCommentCount() {
        this.currentCommentCount += CURRENT_COMMENT_COUNT_INCREASE_UNIT;
    }
}
