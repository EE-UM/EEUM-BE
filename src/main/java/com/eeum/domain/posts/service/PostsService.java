package com.eeum.domain.posts.service;

import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.dto.response.*;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.repository.*;
import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.dto.request.UpdatePostRequest;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.exception.NoAvailablePostsException;
import com.eeum.domain.posts.exception.PostsNotFoundException;
import com.eeum.domain.view.service.ViewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostsService {

    private final PostsRepository postsRepository;
    private final PostsQueryModelRepository postsQueryModelRepository;
    private final PostsIdListRepository postsIdListRepository;
    private final CommentRepository commentRepository;
    private final CommentCountRepository commentCountRepository;
    private final ViewService viewService;
    private final PostsRandomShakeRepository postsRandomShakeRepository;

    @Transactional
    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        validateInvalidAutoCompletion(createPostRequest);

        Album album = Album.of(createPostRequest.albumName(), createPostRequest.songName(), createPostRequest.artistName(), createPostRequest.artworkUrl(), createPostRequest.appleMusicUrl());
        Posts posts = Posts.of(createPostRequest.title(), createPostRequest.content(), album, userId);
        posts.updateCompletionType(createPostRequest.completionType());
        Posts savedPost = postsRepository.save(posts);

        createPostCommentCount(savedPost, createPostRequest.commentCountLimit());

        addRedisRandomPool(savedPost);

        return CreatePostResponse.of(posts.getId(), userId);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long userId, UpdatePostRequest updatePostRequest) {
        Posts posts = postsRepository.findByIdAndUserId(updatePostRequest.postId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find the post."));

        posts.update(updatePostRequest.title(), updatePostRequest.content());

        if (!posts.getIsCompleted() && !posts.getIsDeleted()) {
            addRedisRandomPool(posts);
        } else {
            postsRandomShakeRepository.removeCandidate(String.valueOf(posts.getId()));
        }

        return UpdatePostResponse.from(posts);
    }

    @Transactional
    public Long delete(Long userId, Long postId) {
        Posts posts = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Can not find the post."));
        if (Objects.equals(posts.getUserId(), userId)) {
            postsRepository.deleteById(postId);

            postsRandomShakeRepository.removeCandidate(String.valueOf(postId));
        }
        return postId;
    }

    @Transactional
    public ShowRandomStoryOnShakeResponse showRandomStoryOnShake(Long userId) {
        Random random = new Random();

        List<Long> postIds = postsRepository.findAllIdsIsNotCompletedPosts(userId);

        if (postIds.isEmpty()) {
            throw new NoAvailablePostsException();
        }

        Long pickedPostId = postIds.get(random.nextInt(postIds.size()));

        Posts posts = postsRepository.findById(pickedPostId)
                .orElseThrow(() -> new NullPointerException("Posts repository is empty."));

        viewService.increase(posts.getId(), userId);

        return new ShowRandomStoryOnShakeResponse(String.valueOf(posts.getId()), String.valueOf(posts.getUserId()), posts.getTitle(), posts.getContent());
    }

    @Transactional
    public ShowRandomStoryOnShakeResponse showRandomStoryOnShake2(Long userId) {
        ShowRandomStoryOnShakeResponse showRandomStoryOnShakeResponse = postsRandomShakeRepository.pickRandom().orElseThrow(NoAvailablePostsException::new);
        viewService.increase(Long.parseLong(showRandomStoryOnShakeResponse.postId()), userId);
        return showRandomStoryOnShakeResponse;
    }

    public List<GetMyPostsResponse> getMyPosts(Long userId) {
        List<Posts> posts = postsRepository.findByUserId(userId);
        return posts.stream().map(post -> new GetMyPostsResponse(
                String.valueOf(post.getUserId()),
                post.getTitle(),
                post.getAlbum().getArtworkUrl(),
                post.getIsCompleted()
        )).toList();
    }

    @Transactional
    public PostsReadResponse read(Long userId, Long postId) {
        PostsQueryModel postsQueryModel = postsQueryModelRepository.read(postId)
                .or(() -> fetch(postId))
                .orElseThrow(() -> new IllegalArgumentException("The post is not exist. postId=" + postId));

        List<Comment> comments = commentRepository.findAllByPostsId(postId);
        List<CommentResponse> commentResponse = comments.stream().map(CommentResponse::from).toList();

        viewService.increase(postId, userId);

        return PostsReadResponse.from(postsQueryModel, commentResponse);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollIng(Long pageSize, Long lastPostId) {
        return readAll(
                readAllInfiniteScrollPostsIds(lastPostId, pageSize)
        );
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollDone(Long pageSize, Long lastPostId) {
        return readAll(
                readAllInfiniteScrollPostsIdsDone(lastPostId, pageSize)
        );
    }

    @Transactional
    public CompletePostResponse completePost(Long userId, Long postId) {
        log.info("userId = {}", userId);
        log.info("postId = {}", postId);
        Posts posts = postsRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Can't find a post written by the userId."));

        posts.updateIsCompleted();

        return CompletePostResponse.of(posts.getId(), posts.getUserId(), posts.getIsCompleted());
    }

    public List<GetLikedPostsResponse> getLikedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsLikedByUserId(userId);

        List<GetLikedPostsResponse> response = posts.stream().map(GetLikedPostsResponse::from).toList();
        return response;
    }

    public List<GetCommentedPostsResponse> getCommentedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsCommentedByUserId(userId);

        List<GetCommentedPostsResponse> response = posts.stream().map(GetCommentedPostsResponse::from).toList();
        return response;
    }

    private void addRedisRandomPool(Posts savedPost) {
        postsRandomShakeRepository.addCandidate(new ShowRandomStoryOnShakeResponse(
                String.valueOf(savedPost.getId()),
                String.valueOf(savedPost.getUserId()),
                savedPost.getTitle(),
                savedPost.getContent()
        ));
    }

    private static void validateInvalidAutoCompletion(CreatePostRequest createPostRequest) {
        if (createPostRequest.completionType().equals(CompletionType.AUTO_COMPLETION) && createPostRequest.commentCountLimit() == null) {
            throw new IllegalArgumentException("Comment count limit must not be null when the post is set to auto-complete.");
        }
    }

    private void createPostCommentCount(Posts savedPost, Long commentCountLimit) {
        CommentCount commentCount = CommentCount.of(savedPost.getId(), 0L, commentCountLimit == null ? 0L : commentCountLimit);
        commentCountRepository.save(commentCount);
    }

    private Optional<PostsQueryModel> fetch(Long postId) {
        Optional<PostsQueryModel> postsQueryModelOptional = Optional.ofNullable(postsRepository.findById(postId)
                .map(post -> PostsQueryModel.create(post))
                .orElseThrow(PostsNotFoundException::new));
        log.info("[fetch] postId={}, fetched={}, model={}", postId, postsQueryModelOptional.isPresent(), postsQueryModelOptional);

        postsQueryModelOptional
                .ifPresent(postsQueryModel -> {
                    log.info("[fetch] caching key={}, value={}", postsQueryModel.getPostId(), postsQueryModel);
                    postsQueryModelRepository.create(postsQueryModel, Duration.ofSeconds(60));
                });
        log.info("[PostsReadService.fetch] fetch data. postId={}, isPresent={}", postId, postsQueryModelOptional.isPresent());
        return postsQueryModelOptional;
    }

    private List<PostsReadInfiniteScrollResponse> readAll(List<Long> postsIds) {
        log.info("[PostsReadService.readAll] input postIds: {}", postsIds);
        Map<Long, PostsQueryModel> postsQueryModelMap = postsQueryModelRepository.readAll(postsIds);
        log.info("[PostsReadService.readAll] cached postsMap keys: {}", postsQueryModelMap.keySet());

        List<PostsReadInfiniteScrollResponse> result = postsIds.stream()
                .map(postId -> {
                    Long unpaddedId = postId;
                    PostsQueryModel model = postsQueryModelMap.containsKey(unpaddedId)
                            ? postsQueryModelMap.get(unpaddedId)
                            : fetch(postId).orElse(null);

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

    private List<Long> readAllInfiniteScrollPostsIds(Long lastPostId, Long pageSize) {
        List<Long> postsIds = postsIdListRepository.readAllInfiniteScroll(lastPostId, pageSize);
        log.info("[PostsReadService.readAllInfiniteScrollPostsIds] Redis postIds: {}", postsIds);

        if (pageSize == postsIds.size()) {
            log.info("[PostsReadService.readAllInfiniteScrollPostsIds] return redis data.");
            return postsIds;
        }

        List<ReadAllInfiiniteScrollResponse> originPosts = readAllInfiiniteScroll(lastPostId, pageSize);
        originPosts.forEach(post ->
                postsIdListRepository.add(post.postId(), pageSize)
        );

        log.info("[PostsReadService.readAllInfiniteScrollPostsIds] return origin data.");
        return originPosts.stream()
                .map(ReadAllInfiiniteScrollResponse::postId)
                .toList();
    }

    private List<Long> readAllInfiniteScrollPostsIdsDone(Long lastPostId, Long pageSize) {
        List<Long> postsIds = postsIdListRepository.readAllInfiniteScrollDone(lastPostId, pageSize);
        log.info("[PostsReadService.readAllInfiniteScrollPostsIdsDone] Redis postIds: {}", postsIds);

        if (pageSize == postsIds.size()) {
            log.info("[PostsReadService.readAllInfiniteScrollPostsIdsDone] return redis data.");
            return postsIds;
        }

        List<ReadAllInfiiniteScrollResponse> originPosts = readAllInfiiniteScrollDone(lastPostId, pageSize);
        originPosts.forEach(post ->
                postsIdListRepository.addDone(post.postId(), pageSize)
        );

        log.info("[PostsReadService.readAllInfiniteScrollPostsIdsDone] return origin data.");
        return originPosts.stream()
                .map(ReadAllInfiiniteScrollResponse::postId)
                .toList();
    }

    private List<ReadAllInfiiniteScrollResponse> readAllInfiiniteScroll(Long lastPostId, Long pageSize) {
        List<Posts> posts = lastPostId == null ?
                postsRepository.findAllInfiniteScroll(pageSize) :
                postsRepository.findAllInfiniteScroll(pageSize, lastPostId);
        return posts.stream().map(ReadAllInfiiniteScrollResponse::from).toList();
    }

    private List<ReadAllInfiiniteScrollResponse> readAllInfiiniteScrollDone(Long lastPostId, Long pageSize) {
        List<Posts> posts = lastPostId == null ?
                postsRepository.findAllInfiniteScrollDone(pageSize) :
                postsRepository.findAllInfiniteScrollDone(pageSize, lastPostId);
        return posts.stream().map(ReadAllInfiiniteScrollResponse::from).toList();
    }
}
