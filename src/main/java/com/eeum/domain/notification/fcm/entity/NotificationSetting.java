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

@Table(name = "notification_setting",
    indexes = {
        @Index(name = "uk_notification_setting_user_id_type", columnList = "userId, type", unique = true)
    })
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @Tsid
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public static NotificationSetting createDefault(Long userId, NotificationType type) {
        LocalDateTime now = LocalDateTime.now();
        return NotificationSetting.builder()
            .userId(userId)
            .type(type)
            .enabled(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    @Builder
    public NotificationSetting(Long userId, NotificationType type, boolean enabled,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.type = type;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
