package com.eeum.domain.notification.fcm.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeadLetterMessage {

    @Id
    @Tsid
    private Long id;

    private String queueName;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private int tryCount;

    private boolean resolved;

    private LocalDateTime failedAt;

    public static DeadLetterMessage of(String queueName, String payload, String errorMessage) {
        return DeadLetterMessage
            .builder()
            .queueName(queueName)
            .payload(payload)
            .errorMessage(errorMessage)
            .build();
    }

    @Builder
    public DeadLetterMessage(String queueName, String payload, String errorMessage) {
        this.queueName = queueName;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.tryCount = 0;
        this.resolved = Boolean.FALSE;
        this.failedAt = LocalDateTime.now();
    }
}
