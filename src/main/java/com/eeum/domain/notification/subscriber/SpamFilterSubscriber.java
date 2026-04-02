package com.eeum.domain.notification.subscriber;

import com.eeum.domain.common.spamfilter.service.SpamFilterService;
import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpamFilterSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SpamFilterService spamFilterService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonBody = new String(message.getBody());
            SpamFilterRequest request = objectMapper.readValue(jsonBody, SpamFilterRequest.class);
            spamFilterService.spamPostFilter(request.postId(), request.content());
            log.info("[SpamFilterSubscriber] 스팸 필터 처리 완료 - postId={}", request.postId());
        } catch (JsonProcessingException e) {
            log.error("[SpamFilterSubscriber] 메시지 역직렬화 실패", e);
        } catch (Exception e) {
            log.error("[SpamFilterSubscriber] 스팸 필터 처리 실패", e);
        }
    }
}
