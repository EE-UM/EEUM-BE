package com.eeum.domain.posts.service;

import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import com.eeum.domain.posts.gateway.MusicSearchGateway;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleMusicService {

  private final MusicSearchGateway musicSearchGateway;

  public Collection<AlbumSearchResponse> search(String term, String types, String limit) {
    return musicSearchGateway.search(term, types, limit);
  }
}
