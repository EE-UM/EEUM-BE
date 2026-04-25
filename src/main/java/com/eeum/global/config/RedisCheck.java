package com.eeum.global.config;

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
            String value = redisTemplate.opsForValue().get("redis-check");

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

