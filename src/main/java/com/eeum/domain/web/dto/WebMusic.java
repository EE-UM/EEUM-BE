package com.eeum.domain.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 노래 검색(Apple Music)에서 고른 곡. 폼의 hidden 필드로 넘어온다.
 */
public record WebMusic(
    @NotBlank(message = "노래를 골라 주세요.") String albumName,
    @NotBlank(message = "노래를 골라 주세요.") String songName,
    @NotBlank(message = "노래를 골라 주세요.") String artistName,
    @NotBlank(message = "노래를 골라 주세요.") String artworkUrl,
    @NotBlank(message = "노래를 골라 주세요.") String appleMusicUrl
) {

    public static WebMusic empty() {
        return new WebMusic(null, null, null, null, null);
    }
}
