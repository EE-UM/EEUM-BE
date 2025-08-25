package com.eeum.domain.like.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "lock-like::postId::%d::userId::%d";

    public boolean lock(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    public void releaseLock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (Objects.equals(currentValue, value)) {
            redisTemplate.delete(key);
        }
    }

    public String generateKey(Long postId, Long userId) {
        return KEY.formatted(postId, userId);
    }
}
