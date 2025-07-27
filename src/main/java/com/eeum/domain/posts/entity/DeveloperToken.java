package com.eeum.domain.posts.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
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
    @Tsid
    private Long id;

    private String token;

    private LocalDateTime createdAt = LocalDateTime.now();


    public static DeveloperToken of(String token) {
        LocalDateTime now = LocalDateTime.now();
        return DeveloperToken.builder()
                .token(token)
                .createdAt(now)
                .build();
    }

    @Builder
    private DeveloperToken(String token, LocalDateTime createdAt) {
        this.token = token;
        this.createdAt = createdAt;
    }
}
