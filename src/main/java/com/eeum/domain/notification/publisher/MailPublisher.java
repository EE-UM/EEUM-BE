package com.eeum.domain.notification.publisher;

import com.eeum.domain.notification.dto.request.MailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailPublisher {

    private static final String REPORT_MAIL_MANAGER = "관리자";
    private static final String MAIL_TYPE = "REPORT";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHANNEL_PREFIX = "notification:";

    public void publish(MailRequest mailRequest) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(mailRequest);
            String channel = CHANNEL_PREFIX + REPORT_MAIL_MANAGER;

            redisTemplate.convertAndSend(channel, jsonMessage);
            log.info("알림 발행 완료 - 채널: {}, 타입: {}", channel, MAIL_TYPE);

        } catch (JsonProcessingException e) {
            log.error("Error: 알림 메시지 직렬화 실패. ", e);
        }
    }
}
