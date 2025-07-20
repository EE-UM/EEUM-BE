package com.eeum.like.config;

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
            // 값 조회
            String value = redisTemplate.opsForValue().get("redis-check");

            // 로그 출력
            if (value != null) {
                System.out.println("✅ Redis is working. Value of 'redis-check': " + value);
            } else {
                System.out.println("⚠️ Redis connected, but value not found.");
            }
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
        }
    }
}

