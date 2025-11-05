package com.eeum.domain.notification.dto.request;

public record MailRequest(
        String to,
        String subject,
        String body
) {

    public static MailRequest of(String to, String subject, String body) {
        return new MailRequest(to, subject, body);
    }
}
