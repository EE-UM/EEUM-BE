package com.eeum.domain.notification.fcm.port;

import com.eeum.domain.notification.fcm.port.dto.response.PushResult;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushSender implements PushSender {

    private static final int MULTICAST_LIMIT = 500;
    private static final Set<MessagingErrorCode> INVALID_TOKEN_ERRORS = Set.of(
        MessagingErrorCode.UNREGISTERED,
        MessagingErrorCode.SENDER_ID_MISMATCH
    );
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public PushResult send(List<String> tokens, String title, String body,
        Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return new PushResult(0, List.of());
        }

        int successCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        for (int from = 0; from < tokens.size(); from += MULTICAST_LIMIT) {
            List<String> chunk = tokens.subList(from,
                Math.min(from + MULTICAST_LIMIT, tokens.size()));

            try {
                BatchResponse response = firebaseMessaging.sendEach(
                    buildMessage(chunk, title, body, data));
                successCount += response.getSuccessCount();
                invalidTokens.addAll(extractInvalidTokens(chunk, response.getResponses()));
            } catch (FirebaseMessagingException e) {
                log.error("FCM multicast 전송 실패 - chunkSize: {}, errorCode: {}",
                    chunk.size(), e.getMessagingErrorCode(), e);
            }
        }

        return new PushResult(successCount, invalidTokens);
    }

    private List<Message> buildMessage(List<String> tokens, String title, String body,
        Map<String, String> data) {
        Notification notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build();
        ApnsConfig apnsConfig = ApnsConfig.builder()
            .setAps(Aps.builder()
                .setSound("default")
                .build())
            .build();

        return tokens.stream()
            .map(token -> {
                Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .setApnsConfig(apnsConfig);
                if (data != null && !data.isEmpty()) {
                    builder.putAllData(data);
                }

                return builder.build();
            })
            .toList();
    }

    private List<String> extractInvalidTokens(List<String> tokens, List<SendResponse> responses) {
        List<String> invalidTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (response.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = response.getException();
            MessagingErrorCode errorCode = exception.getMessagingErrorCode();
            if (INVALID_TOKEN_ERRORS.contains(errorCode)) {
                invalidTokens.add(tokens.get(i));
            } else {
                log.warn("FCM 개별 전송 실패 - errorCode: {}, message: {}", errorCode,
                    exception.getMessage());
            }
        }

        return invalidTokens;
    }
}
