package com.eeum.domain.posts.gateway;

import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import java.util.Collection;

public interface MusicSearchGateway {

  Collection<AlbumSearchResponse> search(String term, String types, String limit);
}
