package com.eeum.domain.user.dto.request;

public record UpdateProfileRequest(
        String nickname,
        String email
) {
}
