package com.eeum.domain.notification.producer;

import static com.eeum.global.config.RabbitMQConfig.*;

import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpamFilterProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishSpamFilter(SpamFilterRequest spamFilterRequest) {
        rabbitTemplate.convertAndSend(
                SPAM_FILTER_EXCHANGE,
                SPAM_FILTER_ROUTING_KEY,
                spamFilterRequest
        );

        log.info("[SpamFilterProducer] queued");
    }
}
