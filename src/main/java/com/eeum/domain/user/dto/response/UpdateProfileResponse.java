package com.eeum.domain.user.dto.response;

public record UpdateProfileResponse(
        String nickname,
        String email
) {
    public static UpdateProfileResponse of(String nickname, String email) {
        return new UpdateProfileResponse(nickname, email);
    }
}
