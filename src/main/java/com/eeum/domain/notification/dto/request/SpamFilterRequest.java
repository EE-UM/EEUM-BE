package com.eeum.domain.notification.dto.request;

public record SpamFilterRequest(
        Long postId,
        String content
) {
    public static SpamFilterRequest of(Long id, String content) {
        return new SpamFilterRequest(id, content);
    }
}
