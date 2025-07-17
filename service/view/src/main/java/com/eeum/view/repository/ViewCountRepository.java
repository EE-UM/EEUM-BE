package com.eeum.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ViewCountRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KET_FORMAT = "view::post::%s::view_count";

    public Long read(Long postId) {
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        return result == null ? 0L : Long.valueOf(result);
    }

    public Long increase(Long postId) {
        return redisTemplate.opsForValue().increment(generateKey(postId));
    }

    private String generateKey(Long postid) {
        return KET_FORMAT.formatted(postid);
    }
}
