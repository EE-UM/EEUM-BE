package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.global.redis.RedisFeature;
import com.eeum.global.redis.RedisGuard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostsRandomShakeRepository {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final StringRedisTemplate redisTemplate;
    private final RedisGuard redisGuard;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.redis-key-prefix}")
    private String keyPrefix;

    private static final String KEY_CANDIDATES = "posts::random::candidates";
    private static final String KEY_DATA = "posts::random::data";

    public void addCandidate(ShowRandomStoryOnShakeResponse dto) {
        String pid = String.valueOf(dto.postId());
        String json = serialize(dto);

        redisGuard.run(RedisFeature.SHAKE_POOL, () -> {
            redisTemplate.opsForSet().add(candidatesKey(), pid);
            redisTemplate.opsForHash().put(dataKey(), pid, json);
        });
    }

    public void removeCandidate(String postId) {
        redisGuard.run(RedisFeature.SHAKE_POOL, () -> {
            redisTemplate.opsForSet().remove(candidatesKey(), postId);
            redisTemplate.opsForHash().delete(dataKey(), postId);
        });
    }

    public Optional<ShowRandomStoryOnShakeResponse> pickRandom() {
        return redisGuard.call(RedisFeature.SHAKE_POOL, this::pickRandomFromRedis,
            Optional::empty);
    }

    private Optional<ShowRandomStoryOnShakeResponse> pickRandomFromRedis() {
        String postId = redisTemplate.opsForSet().randomMember(candidatesKey());
        if (postId == null) {
            return Optional.empty();
        }

        Object json = redisTemplate.opsForHash().get(dataKey(), postId);
        if (json == null) {
            return Optional.empty();
        }

        try {
            ShowRandomStoryOnShakeResponse dto =
                objectMapper.readValue(json.toString(), ShowRandomStoryOnShakeResponse.class);
            return Optional.of(dto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void resetAndWarm(Iterable<ShowRandomStoryOnShakeResponse> candidates) {
        redisGuard.run(RedisFeature.SHAKE_POOL, () -> {
            redisTemplate.delete(candidatesKey());
            redisTemplate.delete(dataKey());
        });

        for (ShowRandomStoryOnShakeResponse dto : candidates) {
            addCandidate(dto);
        }
    }

    public Optional<ShowRandomStoryOnShakeResponse> pickRandomExcludingInOneShot(
        Set<Long> blockedUserIds, int candidateSize) {
        return redisGuard.call(RedisFeature.SHAKE_POOL,
            () -> pickRandomExcludingFromRedis(blockedUserIds, candidateSize),
            Optional::empty);
    }

    private Optional<ShowRandomStoryOnShakeResponse> pickRandomExcludingFromRedis(
        Set<Long> blockedUserIds, int candidateSize) {
        Set<String> postIds = redisTemplate.opsForSet()
            .distinctRandomMembers(candidatesKey(), candidateSize);
        if (postIds == null || postIds.isEmpty()) {
            return Optional.empty();
        }

        List<Object> jsonList = redisTemplate.opsForHash()
            .multiGet(dataKey(), new ArrayList<>(postIds));

        return jsonList.stream()
            .filter(Objects::nonNull)
            .map(json -> deserialize(json.toString()))
            .filter(Objects::nonNull)
            .filter(dto -> !blockedUserIds.contains(dto.writerId()))
            .findFirst();
    }

    private String serialize(ShowRandomStoryOnShakeResponse dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to add candidate", e);
        }
    }

    private ShowRandomStoryOnShakeResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, ShowRandomStoryOnShakeResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String candidatesKey() {
        return keyPrefix + KEY_CANDIDATES;
    }

    private String dataKey() {
        return keyPrefix + KEY_DATA;
    }
}
