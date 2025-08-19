package com.eeum.domain.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "view::post::%s::view_count";

    public Long read(Long postId) {
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        return result == null ? 0 : Long.parseLong(result);
    }

    public Long increase(Long postId) {
        return redisTemplate.opsForValue().increment(generateKey(postId));
    }

    private String generateKey(Long postId) {
        return KEY_FORMAT.formatted(postId);
    }
}
