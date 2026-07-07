package com.eeum.domain.posts.gateway;

import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachingDeveloperTokenProvider implements DeveloperTokenProvider {

  private final DeveloperTokenRepository developerTokenRepository;

  @Override
  @Cacheable("developerToken")
  public DeveloperToken getToken() {
    return developerTokenRepository.findTopByOrderByCreatedAtDesc()
        .orElseThrow(() -> new IllegalArgumentException("No valid token found"));
  }
}
