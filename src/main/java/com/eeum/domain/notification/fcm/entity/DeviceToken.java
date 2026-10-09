package com.eeum.domain.notification.fcm.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "device_token",
    indexes = {
        @Index(name = "idx_fcm_token", columnList = "fcmToken", unique = true)
    })
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {

    @Id
    @Tsid
    private Long id;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    private String fcmToken;

    private Long userId;

    public static DeviceToken of(Platform platform, String fcmToken, Long userId) {
        return DeviceToken.builder()
            .platform(platform)
            .fcmToken(fcmToken)
            .userId(userId)
            .build();
    }

    @Builder
    public DeviceToken(Platform platform, String fcmToken, Long userId) {
        this.platform = platform;
        this.fcmToken = fcmToken;
        this.userId = userId;
    }
}
