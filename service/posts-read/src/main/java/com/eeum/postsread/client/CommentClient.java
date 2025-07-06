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
public class CommentClient {

    private RestClient restClient;
    @Value("${endpoints.comment-service.url}")
    private String commentServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(commentServiceUrl);
    }

    public List<CommentResponse> read(Long postId) {
        try {
            ApiResponse<List<CommentResponse>> response = restClient.get()
                    .uri("/comments/{postId}", postId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<CommentResponse>>>() {
                    });
            return response.getData();
        } catch (Exception e) {
            log.error("[CommentClient.read] postId={}", postId);
            return List.of();
        }
    }

    @Getter
    @ToString
    public static class CommentResponse {
        private String commentId;
        private String content;
        private String userId;
        private String username;
        private Boolean isDeleted;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}

