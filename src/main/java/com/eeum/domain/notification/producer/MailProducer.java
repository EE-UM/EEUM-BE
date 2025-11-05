package com.eeum.domain.notification.producer;

import static com.eeum.global.config.RabbitMQConfig.*;

import com.eeum.domain.notification.dto.request.MailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishMail(MailRequest mailRequest) {
        rabbitTemplate.convertAndSend(
                EMAIL_EXCHANGE,
                EMAIL_ROUTING_KEY,
                mailRequest
        );

        log.info("[MailProducer] queued email -> to={}", mailRequest.to());
    }
}
