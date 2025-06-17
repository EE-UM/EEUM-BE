package com.eeum.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "users")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private Long id;

    private String nickname;

    private String username;

    private String email;

    private String role;

    private String provider;

    private String providerId;

    private boolean isRegistered;

    private String fcmToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static User of(Long id, String nickname, String username, String email, String role, String provider, String providerId, boolean isRegistered) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .nickname(nickname)
                .username(username)
                .email(email)
                .role("USER")
                .provider(provider)
                .providerId(providerId)
                .isRegistered(isRegistered)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Builder
    public User(Long id, String nickname, String username, String email, String role, String provider, String providerId, boolean isRegistered, String fcmToken, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.isRegistered = isRegistered;
        this.fcmToken = fcmToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
