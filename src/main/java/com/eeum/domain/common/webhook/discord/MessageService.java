package com.eeum.domain.common.webhook.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MessageService {

    @Value("${discord.webhook-url}")
    String discordWebhookUrl;

    public void sendDiscordWebhookMessage(DiscordWebhookResponse message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordWebhookResponse> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    discordWebhookUrl,
                    HttpMethod.POST,
                    messageEntity,
                    String.class
            );

            if (response.getStatusCode().value() != HttpStatus.NO_CONTENT.value()) {
                log.error("Error : Error occurred after sending the message.");
            }
        } catch (Exception e) {
            log.error("Error : " + e);
        }
    }
}
