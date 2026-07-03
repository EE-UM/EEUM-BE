package com.eeum.domain.posts.service;

import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleMusicService {

  private final DeveloperTokenRepository developerTokenRepository;
  private final DeveloperTokenCacheServie developerTokenCacheServie;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestClient appleMusicRestClient;

  public Collection<AlbumSearchResponse> search(String term, String types, String limit) {
    DeveloperToken developerToken = developerTokenCacheServie.getToken();

    String response = appleMusicRestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v1/catalog/kr/search")
            .queryParam("term", term)
            .queryParam("types", types)
            .queryParam("limit", limit)
            .build())
        .headers(headers -> headers.setBearerAuth(developerToken.getToken()))
        .retrieve()
        .body(String.class);

    return getSearchResponses(types, response);
  }

  private Collection<AlbumSearchResponse> getSearchResponses(String types,
      String response) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode data = root.path("results").path(types).path("data");

      Collection<AlbumSearchResponse> result = new ArrayList<>();

      for (JsonNode item : data) {
        log.info(String.valueOf(item));
        JsonNode attributes = item.path("attributes");
        String albumName = attributes.path("albumName").asText();
        String songName = attributes.path("name").asText();
        String artistName = attributes.path("artistName").asText();
        String imgUrl = attributes.path("artwork").path("url").asText()
            .replace("{w}x{h}", "300x300");
        String previewMusicUrl = attributes.path("previews").get(0).path("url").asText();

        result.add(
            AlbumSearchResponse.of(albumName, songName, artistName, imgUrl, previewMusicUrl));
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse Apple Music response", e);
    }
  }
}
