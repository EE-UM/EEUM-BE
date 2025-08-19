package com.eeum.domain.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class PostViewLockRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "view::post::%s::user::%s::lock";

    public boolean lock(Long postId, Long userId, Duration ttl) {
        String key = generateKey(postId, userId);
        return redisTemplate.opsForValue().setIfAbsent(key, "", ttl);
    }

    private String generateKey(Long postId, Long userId) {
        return KEY_FORMAT.formatted(postId, userId);
    }
}
