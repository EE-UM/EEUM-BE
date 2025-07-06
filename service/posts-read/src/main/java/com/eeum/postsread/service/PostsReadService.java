package com.eeum.postsread.service;

import com.eeum.postsread.client.CommentClient;
import com.eeum.postsread.client.PostsClient;
import com.eeum.postsread.dto.response.PostsReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostsReadService {
    private final PostsClient postsClient;
    private final CommentClient commentClient;

    public PostsReadResponse read(Long postId) {
        PostsClient.PostsResponse postsResponse = postsClient.read(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        List<CommentClient.CommentResponse> commentResponse = commentClient.read(postId);

        log.info(">>> postClient.read(postId={}): {}", postId, postsResponse);
        log.info(">>> commentClient.read(postId={}): {}", postId, commentResponse);

        return PostsReadResponse.of(postsResponse, commentResponse);
    }
}
