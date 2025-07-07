package com.eeum.postsread.service;

import com.eeum.postsread.client.CommentClient;
import com.eeum.postsread.client.PostsClient;
import com.eeum.postsread.dto.response.PostsReadResponse;
import com.eeum.postsread.repository.PostsQueryModel;
import com.eeum.postsread.repository.PostsQueryModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostsReadService {

    private final PostsClient postsClient;
    private final CommentClient commentClient;
    private final PostsQueryModelRepository postsQueryModelRepository;

    public PostsReadResponse read(Long postId) {
        PostsQueryModel postsQueryModel = postsQueryModelRepository.read(postId)
                .or(() -> fetch(postId))
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

//        PostsClient.PostsResponse postsResponse = postsClient.read(postId)
//                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        List<CommentClient.CommentResponse> commentResponse = commentClient.read(postId);

        return PostsReadResponse.from(postsQueryModel, commentResponse);
    }

    private Optional<PostsQueryModel> fetch(Long postId) {
        Optional<PostsQueryModel> postsQueryModelOptional = postsClient.read(postId)
                .map(post -> PostsQueryModel.create(post));
        postsQueryModelOptional
                .ifPresent(postsQueryModel -> postsQueryModelRepository.create(postsQueryModel, Duration.ofDays(1)));
        log.info("[PostsReadService.fetch] fetch data. postId={}, isPresent={}", postId, postsQueryModelOptional.isPresent());
        return postsQueryModelOptional;
    }
}
