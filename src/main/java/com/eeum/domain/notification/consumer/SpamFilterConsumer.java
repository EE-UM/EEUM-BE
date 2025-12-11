package com.eeum.domain.notification.consumer;

import com.eeum.domain.common.spamfilter.service.SpamFilterService;
import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import com.eeum.global.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpamFilterConsumer {

    private static final int MAX_RETRIES = 3;
    private final SpamFilterService spamFilterService;

    private final ConcurrentHashMap<Long, Integer> retries = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.SPAM_FILTER_QUEUE, containerFactory = "mannualAckListenerContainerFactory")
    public void consumeSpamFilter(SpamFilterRequest spamFilterRequest, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            spamFilterService.spamPostFilter(spamFilterRequest.postId(), spamFilterRequest.content());
            channel.basicAck(tag, false);
            retries.remove(spamFilterRequest.postId());
            log.info("[SpamFilterConsumer] SpamFilter execute successfully");
        } catch (Exception e) {
            int retryCount = retries.merge(spamFilterRequest.postId(), 1, Integer::sum);
            log.warn("[SpamFilterConsumer] SpamFilter execute failed, retryCount={}", retryCount, e);

            try {
                if (retryCount < MAX_RETRIES) {
                    channel.basicReject(tag, true);
                } else {
                    channel.basicNack(tag, false, false);
                    retries.remove(spamFilterRequest.postId());
                    log.error("[SpamFilterConsumer] DLQ push");
                }
            } catch (IOException ioException) {
                log.error("[SpamFilterConsumer] ACK/NACK error", ioException);
            }
        }
    }
}
