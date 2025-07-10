package com.eeum.postsread.service;

import com.eeum.postsread.client.CommentClient;
import com.eeum.postsread.client.PostsClient;
import com.eeum.postsread.dto.response.PostsReadInfiniteScrollResponse;
import com.eeum.postsread.dto.response.PostsReadResponse;
import com.eeum.postsread.repository.PostsIdListRepository;
import com.eeum.postsread.repository.PostsQueryModel;
import com.eeum.postsread.repository.PostsQueryModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostsReadService {

    private final PostsClient postsClient;
    private final CommentClient commentClient;
    private final PostsQueryModelRepository postsQueryModelRepository;
    private final PostsIdListRepository postsIdListRepository;

    public PostsReadResponse read(Long postId) {
        PostsQueryModel postsQueryModel = postsQueryModelRepository.read(postId)
                .or(() -> fetch(postId))
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

//        PostsClient.PostsResponse postsResponse = postsClient.read(postId)
//                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        List<CommentClient.CommentResponse> commentResponse = commentClient.read(postId);

        return PostsReadResponse.from(postsQueryModel, commentResponse);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScroll(Long lastPostId, Long pageSize) {
        return readAll(
                readAllInfiniteScrollPostsIds(lastPostId, pageSize)
        );
    }

    private Optional<PostsQueryModel> fetch(Long postId) {
        Optional<PostsQueryModel> postsQueryModelOptional = postsClient.read(postId)
                .map(post -> PostsQueryModel.create(post));
        log.info("[fetch] postId={}, fetched={}, model={}", postId, postsQueryModelOptional.isPresent(), postsQueryModelOptional);

        postsQueryModelOptional
                .ifPresent(postsQueryModel -> {
                    log.info("[fetch] caching key={}, value={}", postsQueryModel.getPostId(), postsQueryModel);
                    postsQueryModelRepository.create(postsQueryModel, Duration.ofDays(1));
                });
        log.info("[PostsReadService.fetch] fetch data. postId={}, isPresent={}", postId, postsQueryModelOptional.isPresent());
        return postsQueryModelOptional;
    }

    private List<PostsReadInfiniteScrollResponse> readAll(List<String> postsIds) {
        log.info("[PostsReadService.readAll] input postIds: {}", postsIds);
        Map<Long, PostsQueryModel> postsQueryModelMap = postsQueryModelRepository.readAll(postsIds);
        log.info("[PostsReadService.readAll] cached postsMap keys: {}", postsQueryModelMap.keySet());

        List<PostsReadInfiniteScrollResponse> result = postsIds.stream()
                .map(postId -> {
                    Long unpaddedId = Long.parseLong(postId);
                    PostsQueryModel model = postsQueryModelMap.containsKey(unpaddedId)
                            ? postsQueryModelMap.get(unpaddedId)
                            : fetch(Long.parseLong(postId)).orElse(null);

                    if (model == null) {
                        log.warn("[readAll] model is null for postId={}", postId);
                    } else {
                        log.info("[readAll] found model for postId={}: {}", postId, model);
                    }
                    return model;
                })
                .filter(Objects::nonNull)
                .map(postsQueryModel -> {
                    try {
                        PostsReadInfiniteScrollResponse dto = PostsReadInfiniteScrollResponse.from(postsQueryModel);
                        log.info("[readAll] successfully converted DTO for postId={}", postsQueryModel.getPostId());
                        return dto;
                    } catch (Exception e) {
                        log.error("[readAll] failed to convert DTO for postId={}", postsQueryModel.getPostId(), e);
                        return null;
                    }
                })
                .toList();

        log.info("[PostsReadService.readAll] final response size: {}", result.size());
        return result;
    }

    private List<String> readAllInfiniteScrollPostsIds(Long lastPostId, Long pageSize) {
        List<String> postsIds = postsIdListRepository.readAllInfiniteScroll(lastPostId, pageSize);
        log.info("[PostsReadService.readAllInfiniteScrollPostsIds] Redis postIds: {}", postsIds);

        if (pageSize == postsIds.size()) {
            log.info("[PostsReadService.readAllInfiniteScrollPostsIds] return redis data.");
            return postsIds;
        }

        List<PostsClient.PostsResponse> originPosts = postsClient.readAllInfiniteScroll(lastPostId, pageSize);
        originPosts.forEach(post ->
                postsIdListRepository.add(Long.parseLong(post.getPostId()), pageSize)
        );

        log.info("[PostsReadService.readAllInfiniteScrollPostsIds] return origin data.");
        return originPosts.stream()
                .map(PostsClient.PostsResponse::getPostId)
                .toList();
    }
}
