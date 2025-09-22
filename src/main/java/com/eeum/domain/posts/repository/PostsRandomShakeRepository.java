package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostsRandomShakeRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KEY_CANDIDATES = "posts::random::candidates";
    private static final String KEY_DATA = "posts::random::data";

    public void addCandidate(ShowRandomStoryOnShakeResponse dto) {
        try {
            String pid = String.valueOf(dto.postId());
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForSet().add(KEY_CANDIDATES, pid);
            redisTemplate.opsForHash().put(KEY_DATA, pid, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to add candidate", e);
        }
    }

    public void removeCandidate(String postId) {
        redisTemplate.opsForSet().remove(KEY_CANDIDATES, postId);
        redisTemplate.opsForHash().delete(KEY_DATA, postId);
    }

    public Optional<ShowRandomStoryOnShakeResponse> pickRandom() {
        String postId = redisTemplate.opsForSet().randomMember(KEY_CANDIDATES);
        if (postId == null) return Optional.empty();

        Object json = redisTemplate.opsForHash().get(KEY_DATA, postId);
        if (json == null) return Optional.empty();

        try {
            ShowRandomStoryOnShakeResponse dto =
                    objectMapper.readValue(json.toString(), ShowRandomStoryOnShakeResponse.class);
            return Optional.of(dto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void resetAndWarm(Iterable<ShowRandomStoryOnShakeResponse> candidates) {
        redisTemplate.delete(KEY_CANDIDATES);
        redisTemplate.delete(KEY_DATA);

        for (ShowRandomStoryOnShakeResponse dto : candidates) {
            addCandidate(dto);
        }
    }
}
