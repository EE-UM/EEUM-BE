package com.eeum.domain.posts.repository;

import com.eeum.global.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostsQueryModelRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "posts::%s";

    public void create(PostsQueryModel postsQueryModel, Duration ttl) {
        String key = generateKey(postsQueryModel.getPostId());
        String value = DataSerializer.serialize(postsQueryModel);
        log.info("[PostsQueryModelRepository.create] key={}, value={}", key, value);
        redisTemplate.opsForValue()
                .set(key, value, ttl);
    }

    public void update(PostsQueryModel postsQueryModel) {
        redisTemplate.opsForValue().setIfPresent(generateKey(postsQueryModel.getPostId()), DataSerializer.serialize(postsQueryModel));
    }

    public void delete(Long postId) {
        redisTemplate.delete(generateKey(postId));
    }

    public Optional<PostsQueryModel> read(Long postId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(postId))
        ).map(json -> DataSerializer.deserialize(json, PostsQueryModel.class));
    }


    private String generateKey(Long postId) {
        return KEY_FORMAT.formatted(postId);
    }

    public Map<Long, PostsQueryModel> readAll(List<Long> postsIds) {
        List<String> keyList = postsIds.stream()
                .map(paddedId -> generateKey(paddedId))
                .toList();
        return redisTemplate.opsForValue().multiGet(keyList).stream()
                .filter(Objects::nonNull)
                .map(json -> DataSerializer.deserialize(json, PostsQueryModel.class))
                .collect(Collectors.toMap(PostsQueryModel::getPostId, identity()));
    }
}
