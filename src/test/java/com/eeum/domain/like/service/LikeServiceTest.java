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

    @Mock
    private RedisLockRepository redisLockRepository;

    private static final Long POST_ID = 100L;
    private static final Long USER_ID = 200L;
    private static final String LOCK_KEY = "lock-like::postId::100::userId::200";

    @Test
    @DisplayName("락 획득 성공 + 좋아요 없을 때 like 호출 시 Like 저장 및 카운트 증가")
    void like_success() {
        given(redisLockRepository.generateKey(POST_ID, USER_ID)).willReturn(LOCK_KEY);
        given(redisLockRepository.lock(eq(LOCK_KEY), any(), eq(Duration.ofSeconds(3)))).willReturn(true);
        given(likeRepository.findByPostIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.empty());
        given(likeCountRepository.increase(POST_ID)).willReturn(1);

        likeService.like(POST_ID, USER_ID);

        verify(likeRepository).save(any(Like.class));
        verify(likeCountRepository).increase(POST_ID);
        verify(redisLockRepository).releaseLock(eq(LOCK_KEY), any());
    }

    @Test
    @DisplayName("락 획득 성공 + LikeCount 행 없을 때 새 LikeCount 저장")
    void like_createsNewLikeCount_whenNoRowUpdated() {
        given(redisLockRepository.generateKey(POST_ID, USER_ID)).willReturn(LOCK_KEY);
        given(redisLockRepository.lock(eq(LOCK_KEY), any(), eq(Duration.ofSeconds(3)))).willReturn(true);
        given(likeRepository.findByPostIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.empty());
        given(likeCountRepository.increase(POST_ID)).willReturn(0);

        likeService.like(POST_ID, USER_ID);

        verify(likeCountRepository).save(any(LikeCount.class));
    }

    @Test
    @DisplayName("락 획득 실패 시 like 호출 시 IllegalArgumentException 발생")
    void like_throwsWhenLockNotAcquired() {
        given(redisLockRepository.generateKey(POST_ID, USER_ID)).willReturn(LOCK_KEY);
        given(redisLockRepository.lock(any(), any(), any())).willReturn(false);

        assertThatThrownBy(() -> likeService.like(POST_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("동일 자원");

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 좋아요가 존재할 때 like 호출 시 RuntimeException 발생")
    void like_throwsWhenAlreadyLiked() {
        Like existingLike = Like.of(USER_ID, POST_ID);
        given(redisLockRepository.generateKey(POST_ID, USER_ID)).willReturn(LOCK_KEY);
        given(redisLockRepository.lock(any(), any(), any())).willReturn(true);
        given(likeRepository.findByPostIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.of(existingLike));

        assertThatThrownBy(() -> likeService.like(POST_ID, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 좋아요");

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("락 획득 실패 시 try 블록 진입 전 예외 발생 → releaseLock 호출되지 않음")
    void like_lockNotAcquired_doesNotReleaseLock() {
        given(redisLockRepository.generateKey(POST_ID, USER_ID)).willReturn(LOCK_KEY);
        given(redisLockRepository.lock(any(), any(), any())).willReturn(false);

        assertThatThrownBy(() -> likeService.like(POST_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class);

        // throw 가 try 블록 밖에서 발생하므로 finally 가 실행되지 않음
        verify(redisLockRepository, never()).releaseLock(any(), any());
    }

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
