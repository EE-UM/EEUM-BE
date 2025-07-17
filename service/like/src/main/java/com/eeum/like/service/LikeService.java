package com.eeum.like.service;

import com.eeum.common.snowflake.Snowflake;
import com.eeum.like.dto.response.LikeResponse;
import com.eeum.like.entity.Like;
import com.eeum.like.entity.LikeCount;
import com.eeum.like.repository.LikeCountRepository;
import com.eeum.like.repository.LikeRepository;
import com.eeum.like.repository.RedisDistributedLockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class LikeService {

    private final Snowflake snowflake = new Snowflake();
    private final LikeRepository likeRepository;
    private final LikeCountRepository likeCountRepository;
    private final RedisDistributedLockRepository redisDistributedLockRepository;

    public LikeResponse read(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(LikeResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("즐겨찾기 내역이 존재하지 않습니다."));
    }

    @Transactional
    public void like(Long postId, Long userId) {
        String lockValue = UUID.randomUUID().toString();
        String lockKey = redisDistributedLockRepository.generateKey(postId, userId);
        boolean acquired = redisDistributedLockRepository.lock(
                lockKey,
                lockValue,
                Duration.ofSeconds(3));

        if (!acquired) {
            throw new IllegalArgumentException("동일 자원에 대한 동시 요청이 발생했습니다. 3초 후 다시 시도해주세요.");
        }

        try {
            Optional<Like> like = likeRepository.findByPostIdAndUserId(postId, userId);

            if (like.isEmpty()) {
                likeRepository.save(
                        Like.of(
                                snowflake.nextId(),
                                userId,
                                postId
                        )
                );
            } else {
                throw new RuntimeException("이미 좋아요를 누른 상태입니다.");
            }

            int result = likeCountRepository.increase(postId);
            if (result == 0) {
                likeCountRepository.save(LikeCount.of(postId, 1L));
            }

        } finally {
            redisDistributedLockRepository.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional
    public void unlike(Long postId, Long userId) {
        likeRepository.findLockedByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    likeCountRepository.decrease(postId);
                });
    }

    public Long count(Long postId) {
        return likeCountRepository.findById(postId)
                .map(LikeCount::getLikeCount)
                .orElse(0L);
    }
}