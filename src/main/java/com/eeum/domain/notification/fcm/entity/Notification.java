package com.eeum.domain.notification.fcm.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "notification",
    indexes = {
        @Index(name = "idx_user_id_created_at", columnList = "userId, createdAt"),
        @Index(name = "idx_user_id_is_read", columnList = "userId, isRead"),
        @Index(name = "idx_target_type_target_id", columnList = "targetType, targetId"),
        @Index(name = "idx_status_created_at", columnList = "status, createdAt ASC")
    })
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Tsid
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;

    private String body;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private boolean isRead;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public void read() {
        if (this.isRead) {
            return;
        }
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public static Notification of(Long userId, NotificationType type, String title, String body,
        TargetType targetType, Long targetId) {
        return Notification.builder()
            .userId(userId)
            .type(type)
            .title(title)
            .body(body)
            .targetType(targetType)
            .targetId(targetId)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Builder
    public Notification(Long userId, NotificationType type, String title, String body,
        TargetType targetType, Long targetId, boolean isRead, LocalDateTime createdAt) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = NotificationStatus.PENDING;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}
