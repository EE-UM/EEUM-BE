package com.eeum.domain.notification.consumer;

import com.eeum.domain.notification.dto.request.MailRequest;
import com.eeum.domain.notification.service.MailService;
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
public class ReportMailConsumer {

    private static final int MAX_RETRIES = 3;
    private final MailService mailService;
    private final ConcurrentHashMap<String, Integer> retries = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE, containerFactory = "mannualAckListenerContainerFactory")
    public void consumeMail(MailRequest mailRequest, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            mailService.sendHtmlMail(mailRequest);
            channel.basicAck(tag, false);
            retries.remove(mailRequest.to());
            log.info("[MailConsumer] Mail sent successfully to={}", mailRequest.to());
        } catch (Exception e) {
            int retryCount = retries.merge(mailRequest.to(), 1, Integer::sum);
            log.warn("[MailConsumer] Mail send failed to={}, retryCount={}", mailRequest.to(), retryCount, e);

            try {
                if (retryCount < MAX_RETRIES) {
                    channel.basicReject(tag, true);
                } else {
                    channel.basicNack(tag, false, false);
                    retries.remove(mailRequest.to());
                    log.error("[MailConsumer] DLQ push for {}", mailRequest.to());
                }
            } catch (IOException ioException) {
                log.error("[MailConsumer] ACK/NACK error", ioException);
            }
        }
    }
}
