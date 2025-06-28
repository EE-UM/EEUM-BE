package com.eeum.posts.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "developer_token")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeveloperToken {

    @Id
    private Long id;

    private String token;

    private LocalDateTime createdAt = LocalDateTime.now();


    public static DeveloperToken of(Long id, String token) {
        LocalDateTime now = LocalDateTime.now();
        return DeveloperToken.builder()
                .id(id)
                .token(token)
                .createdAt(now)
                .build();
    }

    @Builder
    private DeveloperToken(Long id, String token, LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.createdAt = createdAt;
    }
}
