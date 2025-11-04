package com.eeum.domain.common.webhook.discord;

import com.eeum.domain.common.constant.DiscordWebhookType;
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

    @Value("${discord.webhook-report-url}")
    String discordWebhookReportUrl;

    public void sendDiscordWebhookMessage(DiscordWebhookResponse message, DiscordWebhookType discordWebhookType) {
        String url = determineUrl(discordWebhookType);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordWebhookResponse> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
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

    private String determineUrl(DiscordWebhookType discordWebhookType) {
        String url = "";
        if (discordWebhookType == DiscordWebhookType.SIGNUP) url = discordWebhookUrl;
        if (discordWebhookType == DiscordWebhookType.REPORT) url = discordWebhookReportUrl;

        return url;
    }
}
