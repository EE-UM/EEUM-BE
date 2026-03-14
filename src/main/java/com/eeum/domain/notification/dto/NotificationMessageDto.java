package com.eeum.domain.notification.dto;

import java.time.LocalDateTime;

public record NotificationMessageDto(
        String userId,
        String type,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
