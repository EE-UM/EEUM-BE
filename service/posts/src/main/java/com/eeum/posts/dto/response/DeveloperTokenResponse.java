package com.eeum.posts.dto.response;

import com.eeum.posts.entity.DeveloperToken;
import lombok.Builder;

import java.time.LocalDateTime;

public record DeveloperTokenResponse(
        String developerToken,
        LocalDateTime createdAt
) {

    public static DeveloperTokenResponse from(DeveloperToken developerToken) {
        return DeveloperTokenResponse.builder()
                .developerToken(developerToken.getToken())
                .createdAt(developerToken.getCreatedAt())
                .build();
    }

    @Builder
    public DeveloperTokenResponse(String developerToken, LocalDateTime createdAt) {
        this.developerToken = developerToken;
        this.createdAt = createdAt;
    }
}
