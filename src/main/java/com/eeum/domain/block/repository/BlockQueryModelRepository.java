package com.eeum.domain.block.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BlockQueryModelRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "block::%s::%s";

    private String generateKey(Long blockerId, Long blockedUsersId) {
        return KEY_FORMAT.formatted(blockerId, blockedUsersId);
    }
}
