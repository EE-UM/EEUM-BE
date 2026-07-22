package com.eeum.domain.posts.gateway;

import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
import com.eeum.global.infrastructure.applemusickit.AppleMusickit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachingDeveloperTokenProvider implements DeveloperTokenProvider {

  private final DeveloperTokenRepository developerTokenRepository;
  private final AppleMusickit appleMusickit;

  @Override
  @Cacheable(cacheNames = "developerToken", key = "'appleMusic'")
  public DeveloperToken getToken() {
    return developerTokenRepository.findTopByOrderByCreatedAtDesc()
        .orElseGet(this::issueAndSave);
  }

  @Override
  @CacheEvict(cacheNames = "developerToken", key = "'appleMusic'")
  public void evictToken() {
    developerTokenRepository.deleteAll();
  }

  private DeveloperToken issueAndSave() {
    String token = appleMusickit.generateToken();
    return developerTokenRepository.save(DeveloperToken.of(token));
  }
}
