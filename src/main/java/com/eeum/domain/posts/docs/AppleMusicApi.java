package com.eeum.domain.posts.docs;

import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import com.eeum.domain.posts.dto.response.DeveloperTokenResponse;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Tag(name = "Apple Music", description = "Apple Music API")
public interface AppleMusicApi {

    @Operation(summary = "토큰 발급/저장", description = "Apple Music Developer Token을 새로 생성하거나 기존 토큰을 반환합니다.")
    @PostMapping("/token")
    ApiResponse<Long> issueToken();

    @Operation(summary = "토큰 조회", description = "현재 유효한 Apple Music Developer Token을 조회합니다.")
    @GetMapping("/token")
    ApiResponse<DeveloperTokenResponse> getDeveloperToken();

    @Operation(summary = "앨범 검색", description = "Apple Music API를 통해 앨범을 검색합니다.")
    @GetMapping("/search")
    ApiResponse<Collection<AlbumSearchResponse>> search(
            @Parameter(description = "검색어", example = "IU") @RequestParam("term") String term,
            @Parameter(description = "검색 타입 (예: albums, artists, songs)", example = "albums") @RequestParam("types") String types,
            @Parameter(description = "검색 결과 개수 제한", example = "10") @RequestParam("limit") String limit
    );
}
