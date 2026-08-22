package com.eeum.domain.notification.publisher;

import com.eeum.domain.notification.dto.request.MailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailPublisher {

    private static final String CHANNEL = "notification:mail";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(MailRequest mailRequest) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(mailRequest);
            redisTemplate.convertAndSend(CHANNEL, jsonMessage);
            log.info("메일 알림 발행 완료 - 채널: {}", CHANNEL);
        } catch (JacksonException e) {
            log.error("[Error] 알림 메시지 직렬화 실패. ", e);
        }
    }
}
