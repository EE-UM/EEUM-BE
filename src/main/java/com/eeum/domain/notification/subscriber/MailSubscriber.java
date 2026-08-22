package com.eeum.domain.notification.subscriber;

import com.eeum.domain.notification.dto.request.MailRequest;
import com.eeum.domain.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonBody = new String(message.getBody());
            MailRequest mailRequest = objectMapper.readValue(jsonBody, MailRequest.class);
            mailService.sendHtmlMail(mailRequest);
            log.info("[MailSubscriber] 메일 전송 완료 - to={}", mailRequest.to());
        } catch (JacksonException e) {
            log.error("[MailSubscriber] 메시지 역직렬화 실패", e);
        } catch (Exception e) {
            log.error("[MailSubscriber] 메일 전송 실패", e);
        }
    }
}
