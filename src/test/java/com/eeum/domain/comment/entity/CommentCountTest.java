package com.eeum.domain.comment.entity;

import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentCountTest {

    @Test
    @DisplayName("댓글 수가 한도 미만이면 increaseOrThrow 성공 후 카운트 증가")
    void increaseOrThrow_success() {
        CommentCount commentCount = CommentCount.of(1L, 2L, 5L);

        commentCount.increaseOrThrow();

        assertThat(commentCount.getCommentCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("댓글 수가 한도에 도달했을 때 increaseOrThrow 호출 시 예외 발생")
    void increaseOrThrow_throwsWhenAtLimit() {
        CommentCount commentCount = CommentCount.of(1L, 5L, 5L);

        assertThatThrownBy(commentCount::increaseOrThrow)
                .isInstanceOf(AlreadyFinishedPostException.class);
    }

    @Test
    @DisplayName("댓글 수가 0 초과이면 decreaseSafely 호출 시 1 감소")
    void decreaseSafely_decreasesWhenPositive() {
        CommentCount commentCount = CommentCount.of(1L, 3L, 5L);

        commentCount.decreaseSafely();

        assertThat(commentCount.getCommentCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글 수가 0이면 decreaseSafely 호출 후에도 0 유지")
    void decreaseSafely_doesNotGoBelowZero() {
        CommentCount commentCount = CommentCount.of(1L, 0L, 5L);

        commentCount.decreaseSafely();

        assertThat(commentCount.getCommentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("댓글 수가 한도와 같으면 hitLimit 은 true")
    void hitLimit_returnsTrue_whenAtLimit() {
        CommentCount commentCount = CommentCount.of(1L, 5L, 5L);

        assertThat(commentCount.hitLimit()).isTrue();
    }

    @Test
    @DisplayName("댓글 수가 한도 미만이면 hitLimit 은 false")
    void hitLimit_returnsFalse_whenBelowLimit() {
        CommentCount commentCount = CommentCount.of(1L, 3L, 5L);

        assertThat(commentCount.hitLimit()).isFalse();
    }
}
