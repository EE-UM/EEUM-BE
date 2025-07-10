package com.eeum.postsread.client;

import com.eeum.common.response.ApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
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
            ApiResponse<PostsResponse> response = restClient.get()
                    .uri("/posts/{postId}", postId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<PostsResponse>>() {});
            return Optional.ofNullable(response.getData());
        } catch (Exception e) {
            log.error("[PostsClient.read] postId={}", postId);
            return Optional.empty();
        }
    }

    public List<PostsResponse> readAllInfiniteScroll(Long lastPostId, Long pageSize) {
        try {
            ApiResponse<List<PostsResponse>> response = restClient.get()
                    .uri(
                            lastPostId != null ?
                                    "/posts/infinite-scroll?lastPostId=%s&pageSize=%s"
                                            .formatted(lastPostId, pageSize) :
                                    "/posts/infinite-scroll?pageSize=%s"
                                            .formatted(pageSize)
                    )
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<PostsResponse>>>() {
                    });

            return Optional.ofNullable(response.getData()).orElse(List.of());
        } catch (Exception e) {
            log.error("[PostsClient.readAllInfiniteScroll] Error occurred. lastPostId={}, pageSize={}",
                    lastPostId, pageSize, e);
            return List.of();
        }
    }

    @Getter
    @ToString
    public static class PostsResponse {
        private String postId;
        private String title;
        private String content;
        private String userId;
        private String songName;
        private String artistName;
        private String artworkUrl;
        private String appleMusicUrl;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
