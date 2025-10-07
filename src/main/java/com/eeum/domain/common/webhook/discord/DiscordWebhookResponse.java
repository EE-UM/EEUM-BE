package com.eeum.domain.common.webhook.discord;

public record DiscordWebhookResponse(
        String content
) {
    public static DiscordWebhookResponse of(String content) {
        if (content.length() >= 2000) {
            content = content.substring(0, 1993) + "\n...```";
        }
        return new DiscordWebhookResponse(content);
    }
}
