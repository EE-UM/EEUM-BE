package com.eeum.domain.comment.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentProducer {

    private static final String CHANNEL = "notification:playlist";
    private final StringRedisTemplate redisTemplate;

    public void sendCompletedPost(Long postId) {
        redisTemplate.convertAndSend(CHANNEL, String.valueOf(postId));
        log.info("게시물 완료 알림 발행 - 채널: {}, postId: {}", CHANNEL, postId);
    }
}
