package com.eeum.postsread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCheck {

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void testRedis() {
        try {
            redisTemplate.opsForValue().set("redis-check", "true");
            System.out.println("✅ Redis is working");
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
        }
    }
}

