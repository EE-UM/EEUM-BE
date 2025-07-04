package com.eeum.postsread.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostsClient {
    private RestClient restClient;
    @Value("${endpoints.posts-service.url}")
    private String postsServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(postsServiceUrl);
    }

    public Optional<PostsResponse> read(Long postId) {
        try {
            PostsResponse response = restClient.get()
                    .uri("/posts/{postId}", postId)
                    .retrieve()
                    .body(PostsResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("[PostsClient.read] postId={}", postId);
            return Optional.empty();
        }
    }

    @Getter
    public static class PostsResponse {
        private Long postId;
        private String title;
        private String content;
        private String songName;
        private String artistName;
        private String artworkUrl;
        private String appleMusicUrl;
        private LocalDateTime createdAt;
    }
}
