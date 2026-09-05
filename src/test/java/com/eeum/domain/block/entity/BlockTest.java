package com.eeum.domain.block.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockTest {

    @DisplayName("차단하는 계정 id와 차단당하는 계정 id가 다르면 차단에 성공한다.")
    @Test
    void blockSuccessTest() {
        // given
        Long BLOCKER_ID = 1L;
        Long BLOCKED_ID = 2L;

        // when
        Block block = Block.of(BLOCKER_ID, BLOCKED_ID);

        // then
        Assertions.assertThat(block.getBlockedUserId()).isEqualTo(BLOCKED_ID);
        Assertions.assertThat(block.getBlockerUserId()).isEqualTo(BLOCKER_ID);
    }

    @DisplayName("차단하는 계정 id와 차단당하는 계정 id가 같으면 예외를 발생한다.")
    @Test
    void blockerIdAndBlockedIdCannotBeSame() {
        // given
        Long BLOCKER_ID = 1L;
        Long BLOCKED_ID = 1L;

        // when // then
        Assertions.assertThatThrownBy(() -> Block.of(BLOCKER_ID, BLOCKED_ID)).isInstanceOf(
            IllegalArgumentException.class);
    }
}