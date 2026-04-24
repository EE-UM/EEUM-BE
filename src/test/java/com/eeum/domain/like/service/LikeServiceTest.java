package com.eeum.domain.like.service;

import com.eeum.domain.like.entity.Like;
import com.eeum.domain.like.entity.LikeCount;
import com.eeum.domain.like.repository.LikeCountRepository;
import com.eeum.domain.like.repository.LikeRepository;
import com.eeum.domain.like.repository.RedisLockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private LikeCountRepository likeCountRepository;

    private static final Long POST_ID = 100L;
    private static final Long USER_ID = 200L;

    @Test
    @DisplayName("unlike 호출 시 기존 Like 삭제 및 카운트 감소")
    void unlike_deletesLikeAndDecreases() {
        Like existingLike = Like.of(USER_ID, POST_ID);
        given(likeRepository.findLockedByPostIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.of(existingLike));

        likeService.unlike(POST_ID, USER_ID);

        verify(likeRepository).delete(existingLike);
        verify(likeCountRepository).decrease(POST_ID);
    }

    @Test
    @DisplayName("unlike 호출 시 Like 가 없으면 아무 작업도 하지 않음")
    void unlike_doesNothingWhenNoLike() {
        given(likeRepository.findLockedByPostIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.empty());

        likeService.unlike(POST_ID, USER_ID);

        verify(likeRepository, never()).delete(any());
        verify(likeCountRepository, never()).decrease(any());
    }

    @Test
    @DisplayName("count 호출 시 LikeCount 가 존재하면 해당 값 반환")
    void count_returnsLikeCount() {
        LikeCount likeCount = LikeCount.of(POST_ID, 7L);
        given(likeCountRepository.findById(POST_ID)).willReturn(Optional.of(likeCount));

        Long result = likeService.count(POST_ID);

        assertThat(result).isEqualTo(7L);
    }

    @Test
    @DisplayName("count 호출 시 LikeCount 가 없으면 0 반환")
    void count_returnsZeroWhenNotFound() {
        given(likeCountRepository.findById(POST_ID)).willReturn(Optional.empty());

        Long result = likeService.count(POST_ID);

        assertThat(result).isEqualTo(0L);
    }
}
