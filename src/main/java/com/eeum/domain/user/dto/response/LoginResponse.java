package com.eeum.domain.user.dto.response;

import lombok.Builder;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String role,
        boolean isRegistered
) {

    public static LoginResponse of(String accessToken, boolean isRegistered) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .isRegistered(isRegistered)
                .build();
    }

    @Builder
    public LoginResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
        if (role == null) {
            role = "ROLE_USER";
        }
    }
}
