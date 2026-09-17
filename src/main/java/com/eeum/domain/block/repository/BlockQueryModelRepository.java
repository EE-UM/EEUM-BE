package com.eeum.domain.block.repository;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BlockQueryModelRepository {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis-key-prefix}")
    private String keyPrefix;

    private static final String KEY_FORMAT = "block::%s";

    private static final String EMPTY_MARKER = "-";

    private static final Duration TTL = Duration.ofDays(1);

    public void create(Long blockerId, Collection<Long> blockedUserIds) {
        String key = generateKey(blockerId);
        redisTemplate.delete(key);

        if (blockedUserIds.isEmpty()) {
            redisTemplate.opsForSet().add(key, EMPTY_MARKER);
        } else {
            redisTemplate.opsForSet()
                .add(key, blockedUserIds.stream().map(String::valueOf).toArray(String[]::new));
        }
        redisTemplate.expire(key, TTL);
    }

    public Optional<Set<Long>> read(Long blockerId) {
        Set<String> members = redisTemplate.opsForSet().members(generateKey(blockerId));
        if (members == null || members.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(members.stream()
            .filter(member -> !EMPTY_MARKER.equals(member))
            .map(Long::valueOf)
            .collect(Collectors.toCollection(HashSet::new)));
    }

    public void add(Long blockerId, Long blockedUserId) {
        String key = generateKey(blockerId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }

        redisTemplate.opsForSet().remove(key, EMPTY_MARKER);
        redisTemplate.opsForSet().add(key, String.valueOf(blockedUserId));
        redisTemplate.expire(key, TTL);
    }

    public void remove(Long blockerId, Long blockedUserId) {
        String key = generateKey(blockerId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        redisTemplate.opsForSet().remove(key, String.valueOf(blockedUserId));

        Long size = redisTemplate.opsForSet().size(key);
        if (size == null || size == 0L) {
            redisTemplate.opsForSet().add(key, EMPTY_MARKER);
        }
        redisTemplate.expire(key, TTL);
    }

    public void delete(Long blockerId) {
        redisTemplate.delete(generateKey(blockerId));
    }

    private String generateKey(Long blockerId) {
        return keyPrefix + KEY_FORMAT.formatted(blockerId);
    }
}
