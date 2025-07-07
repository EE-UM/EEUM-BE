package com.eeum.postsread.repository;

import com.eeum.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostsQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    // posts-read::posts::{postId}
    private static final String KEY_FORMAT = "posts-read::posts::%s";

    public void create(PostsQueryModel postsQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(postsQueryModel), DataSerializer.serialize(postsQueryModel), ttl);
    }

    public void update(PostsQueryModel postsQueryModel) {
        redisTemplate.opsForValue().setIfPresent(generateKey(postsQueryModel), DataSerializer.serialize(postsQueryModel));
    }

    public void delete(Long postId) {
        redisTemplate.delete(generateKey(postId));
    }

    public Optional<PostsQueryModel> read(Long postId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(postId))
        ).map(json -> DataSerializer.deserialize(json, PostsQueryModel.class));
    }

    private String generateKey(PostsQueryModel postsQueryModel) {
        return generateKey(postsQueryModel.getPostId());
    }

    private String generateKey(Long postId) {
        return KEY_FORMAT.formatted(postId);
    }
}
