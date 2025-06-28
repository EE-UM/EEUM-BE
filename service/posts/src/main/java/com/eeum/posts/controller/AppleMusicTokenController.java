package com.eeum.posts.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.posts.dto.response.AlbumSearchResponse;
import com.eeum.posts.dto.response.DeveloperTokenResponse;
import com.eeum.posts.service.AppleMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apple-music")
public class AppleMusicTokenController {

    private final AppleMusicService appleMusicService;

    @PostMapping("/token")
    public ApiResponse<Long> issueToken() {
        Long tokenId = appleMusicService.getOrCreateToken();
        return ApiResponse.success(tokenId);
    }

    @GetMapping("/token")
    public ApiResponse<DeveloperTokenResponse> getDeveloperToken() {
        DeveloperTokenResponse developerToken = appleMusicService.getDeveloperToken();
        return ApiResponse.success(developerToken);
    }

    @GetMapping("/search")
    public ApiResponse<Collection<AlbumSearchResponse>> search(
            @RequestParam String term,
            @RequestParam String types,
            @RequestParam String limit) {
        Collection<AlbumSearchResponse> result = appleMusicService.search(term, types, limit);
        return ApiResponse.success(result);
    }
}
