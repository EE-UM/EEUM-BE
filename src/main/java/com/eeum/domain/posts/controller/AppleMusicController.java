package com.eeum.domain.posts.controller;

import com.eeum.domain.posts.docs.AppleMusicApi;
import com.eeum.global.support.response.ApiResponse;
import com.eeum.domain.posts.dto.response.DeveloperTokenResponse;
import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import com.eeum.domain.posts.service.AppleMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apple-music")
public class AppleMusicController implements AppleMusicApi {

    private final AppleMusicService appleMusicService;

    @GetMapping("/token")
    public ApiResponse<DeveloperTokenResponse> getDeveloperToken() {
        DeveloperTokenResponse developerToken = appleMusicService.getDeveloperToken();
        return ApiResponse.success(developerToken);
    }

    @GetMapping("/search")
    public ApiResponse<Collection<AlbumSearchResponse>> search(
            @RequestParam("term") String term,
            @RequestParam("types") String types,
            @RequestParam("limit") String limit) {
        Collection<AlbumSearchResponse> result = appleMusicService.search(term, types, limit);
        return ApiResponse.success(result);
    }
}
