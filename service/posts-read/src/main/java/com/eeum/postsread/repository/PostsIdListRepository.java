package com.eeum.postsread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostsIdListRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "posts-read::post-list";

    public void add(Long postId, Long limit) {
        redisTemplate.expire(KEY, Duration.ofSeconds(60));
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            conn.zAdd(KEY, 0, toPaddedString(postId));
            conn.zRemRange(KEY, 0, -limit - 1);

            return null;
        });
    }

    public List<String> readAllInfiniteScroll(Long lastPostId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex(
                KEY,
                lastPostId == null
                        ? Range.unbounded()
                        : Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastPostId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(String::valueOf).toList();
    }

    public void delete(Long postId) {
        redisTemplate.opsForZSet().remove(KEY, toPaddedString(postId));
    }

    private String toPaddedString(Long postId) {
        return "%019d".formatted(postId);
    }
}
