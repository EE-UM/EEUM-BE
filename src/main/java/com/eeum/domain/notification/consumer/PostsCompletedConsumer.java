package com.eeum.domain.notification.consumer;

import com.eeum.global.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostsCompletedConsumer {

    private static final int MAX_RETRIES = 3;
    private final ConcurrentHashMap<Long, Integer> retries = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.PLAYLIST_COMPLETED_QUEUE, containerFactory = "mannualAckListenerContainerFactory")
    public void handle(Long postId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {


            channel.basicAck(tag, false);
            retries.remove(postId);
            log.info("ACK postId={}",postId);
        } catch (Exception e) {
            Integer cnt = retries.merge(postId, 1, Integer::sum);
            log.warn("consume failed postId={}, retryCount={}", postId, cnt, e);

            try {
                if (cnt < MAX_RETRIES) {
                    channel.basicReject(tag, true);
                } else {
                    channel.basicNack(tag, false, false);
                    retries.remove(postId);
                    log.warn("NACK to DLQ postId={}", postId);
                }

            } catch (IOException io) {
                log.error("ACK/NACK error", io);
            }
        }
    }
}
