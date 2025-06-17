package com.eeum.posts.dto.request;

public record CreatePostRequest(
        String title,
        String content
) {
}
