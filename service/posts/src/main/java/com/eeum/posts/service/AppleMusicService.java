package com.eeum.posts.service;

import com.eeum.common.applemusickit.AppleMusickit;
import com.eeum.common.snowflake.Snowflake;
import com.eeum.posts.dto.response.AlbumSearchResponse;
import com.eeum.posts.dto.response.DeveloperTokenResponse;
import com.eeum.posts.entity.DeveloperToken;
import com.eeum.posts.repository.DeveloperTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleMusicService {

    private static final String APPLE_MUSIC_SEARCH_URL = "https://api.music.apple.com/v1/catalog/kr/search";

    private final DeveloperTokenRepository developerTokenRepository;
    private final AppleMusickit appleMusickit;
    private final Snowflake snowflake = new Snowflake();

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeveloperTokenResponse getDeveloperToken() {
        DeveloperToken token = developerTokenRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No valid token found"));

        return DeveloperTokenResponse.from(token);
    }

    @Transactional
    public Long getOrCreateToken() {
        String token = appleMusickit.generateToken();
        DeveloperToken developerToken = DeveloperToken.of(snowflake.nextId(), token);
        DeveloperToken savedToken = developerTokenRepository.save(developerToken);
        return savedToken.getId();
    }

    public Collection<AlbumSearchResponse> search(String term, String types, String limit) {
        DeveloperToken developerToken = developerTokenRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(RuntimeException::new);

        String url = urlBuild(term, types, limit);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(developerToken.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
        System.out.println("response.getBody() = " + response.getBody());
        return getSearchResponses(types, response);
    }

    private Collection<AlbumSearchResponse> getSearchResponses(String types, ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("results").path(types).path("data");

            Collection<AlbumSearchResponse> result = new ArrayList<>();

            for (JsonNode item : data) {
                JsonNode attributes = item.path("attributes");
                String albumName = attributes.path("albumName").asText();
                String songName = attributes.path("name").asText();
                String artistName = attributes.path("artistName").asText();
                String imgUrl = attributes.path("artwork").path("url").asText().replace("{w}x{h}", "300x300");

                result.add(new AlbumSearchResponse(albumName, songName, artistName, imgUrl));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Apple Music response", e);
        }
    }

    private static String urlBuild(String term, String types, String limit) {
        String url = UriComponentsBuilder
                .fromHttpUrl(APPLE_MUSIC_SEARCH_URL)
                .queryParam("term", term)
                .queryParam("types", types)
                .queryParam("limit", limit)
                .build()
                .toUriString();
        return url;
    }
}
