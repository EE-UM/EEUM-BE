package com.eeum.domain.posts.service;

import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeveloperTokenCacheServie {

    private final DeveloperTokenRepository developerTokenRepository;

    @Cacheable("developerToken")
    public DeveloperToken getToken() {
        return developerTokenRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalArgumentException("No valid token found"));
    }
}
