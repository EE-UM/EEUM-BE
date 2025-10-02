package com.eeum.domain.user.dto.response;

public record GetProfileResponse(
        String nickname,
        String email
) {
    public static GetProfileResponse of(String nickname, String email) {
        return new GetProfileResponse(nickname, email);
    }
}
