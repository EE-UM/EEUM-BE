package com.eeum.domain.posts.gateway;

import com.eeum.domain.posts.dto.response.AlbumSearchResponse;
import com.eeum.domain.posts.entity.DeveloperToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class AppleMusicSearchGateway implements MusicSearchGateway {

  private final RestClient appleMusicClient;
  private final DeveloperTokenProvider developerTokenProvider;
  private final ObjectMapper objectMapper;

  public AppleMusicSearchGateway(RestClient appleMusicClient,
      DeveloperTokenProvider developerTokenProvider, ObjectMapper objectMapper) {
    this.appleMusicClient = appleMusicClient;
    this.developerTokenProvider = developerTokenProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  public Collection<AlbumSearchResponse> search(String term, String types, String limit) {
    DeveloperToken token = developerTokenProvider.getToken();

    String response;

    try {
      response = callAppleMusic(term, types, limit, token);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        developerTokenProvider.evictToken();
      }

      log.error(
          "[AppleMusicSearchGateway.search] Apple Music API error. status={}, term={}, body={}",
          e.getStatusCode(), term, e.getResponseBodyAsString(), e);

      throw e;
    }

    return getSearchResponses(types, response);
  }

  private @Nullable String callAppleMusic(String term, String types, String limit,
      DeveloperToken token) {
    return appleMusicClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v1/catalog/kr/search")
            .queryParam("term", term)
            .queryParam("types", types)
            .queryParam("limit", limit)
            .build())
        .headers(headers -> headers.setBearerAuth(token.getToken()))
        .retrieve()
        .body(String.class);
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
      log.error(
          "[AppleMusicSearchGateway.getSearchResponses] Failed to parse response. types={}, response={}",
          types, response, e);
      throw new RuntimeException("Failed to parse Apple Music response", e);
    }
  }
}
