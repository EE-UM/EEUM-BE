package com.eeum.domain.notification.publisher;

import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpamFilterPublisher {

    private static final String CHANNEL = "notification:spamfilter";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(SpamFilterRequest spamFilterRequest) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(spamFilterRequest);
            redisTemplate.convertAndSend(CHANNEL, jsonMessage);
            log.info("스팸 필터 알림 발행 완료 - 채널: {}", CHANNEL);
        } catch (JacksonException e) {
            log.error("[Error] 스팸 알림 메시지 직렬화 실패. ", e);
        }
    }
}
