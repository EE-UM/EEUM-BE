package com.eeum.postsread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostsQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "posts-read::posts::%s";
}
