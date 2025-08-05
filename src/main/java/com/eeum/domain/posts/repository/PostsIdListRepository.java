package com.eeum.domain.posts.repository;

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
    private static final String DONE_KEY = "posts-read::post-list::done";

    public void add(Long postId, Long limit) {
        redisTemplate.expire(KEY, Duration.ofSeconds(60));
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            conn.zAdd(KEY, 0, toPaddedString(postId));
            conn.zRemRange(KEY, 0, -limit - 1);

            return null;
        });
    }

    public void addDone(Long postId, Long limit) {
        redisTemplate.expire(DONE_KEY, Duration.ofSeconds(60));
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            conn.zAdd(DONE_KEY, 0, toPaddedString(postId));
            conn.zRemRange(DONE_KEY, 0, -limit - 1);

            return null;
        });
    }

    public List<Long> readAllInfiniteScroll(Long lastPostId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex(
                KEY,
                lastPostId == null
                        ? Range.unbounded()
                        : Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastPostId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(Long::valueOf).toList();
    }

    public List<Long> readAllInfiniteScrollDone(Long lastPostId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex(
                DONE_KEY,
                lastPostId == null
                        ? Range.unbounded()
                        : Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastPostId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(Long::valueOf).toList();
    }

    public void delete(Long postId) {
        redisTemplate.opsForZSet().remove(KEY, toPaddedString(postId));
    }

    private String toPaddedString(Long postId) {
        return "%019d".formatted(postId);
    }
}
