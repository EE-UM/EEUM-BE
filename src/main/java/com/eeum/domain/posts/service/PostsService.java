package com.eeum.domain.posts.service;

import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.like.repository.LikeRepository;
import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import com.eeum.domain.notification.producer.SpamFilterProducer;
import com.eeum.domain.posts.dto.response.*;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.entity.PostsCommentCount;
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
    private final LikeRepository likeRepository;
    private final ViewService viewService;
    private final PostsRandomShakeRepository postsRandomShakeRepository;
    private final PostsCommentCountRepository postsCommentCountRepository;

    private final SpamFilterProducer spamFilterProducer;

    @Transactional
    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        validateInvalidAutoCompletion(createPostRequest);

        Album album = Album.of(createPostRequest.albumName(), createPostRequest.songName(),
                createPostRequest.artistName(), createPostRequest.artworkUrl(), createPostRequest.appleMusicUrl());
        Posts posts = Posts.of(createPostRequest.title(), createPostRequest.content(), album, userId);
        posts.updateCompletionType(createPostRequest.completionType());
        Posts savedPost = postsRepository.save(posts);

        createPostCommentCount(savedPost, createPostRequest.commentCountLimit());

        addRedisRandomPool(savedPost);

        spamFilterProducer.publishSpamFilter(SpamFilterRequest.of(posts.getId(), posts.getContent()));

        createPostsCommentCount(createPostRequest, posts);

        return CreatePostResponse.of(posts.getId(), userId);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long userId, UpdatePostRequest updatePostRequest) {
        Posts posts = postsRepository.findByIdAndUserId(updatePostRequest.postId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find the post."));

        posts.update(updatePostRequest.title(), updatePostRequest.content());

        if (!posts.getIsCompleted()) {
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
            throw new IllegalArgumentException("Only the author can delete this post.");
        }

        postsRepository.deleteById(postId);
        postsRandomShakeRepository.removeCandidate(String.valueOf(postId));
        return postId;
    }

    @Transactional
    public ShowRandomStoryOnShakeResponse showRandomStoryOnShake() {
        ShowRandomStoryOnShakeResponse showRandomStoryOnShakeResponse = postsRandomShakeRepository.pickRandom()
                .orElseThrow(NoAvailablePostsException::new);
        return showRandomStoryOnShakeResponse;
    }

    public GetMyPostsResponse getMyPosts(Long userId) {
        List<Posts> posts = postsRepository.findByUserId(userId);

        Long postCount = (long) posts.size();
        List<GetMyPostResponse> getMyPostResponse = posts.stream().map(post -> {
            PostsCommentCount postsCommentCount = postsCommentCountRepository.findByPostId(post.getId())
                    .orElseThrow(IllegalArgumentException::new);
            GetMyPostResponse test = new GetMyPostResponse(post.getId(), post.getTitle(),
                    post.getAlbum().getArtworkUrl(), post.getIsCompleted(),
                    postsCommentCount.getCurrentCommentCount(), postsCommentCount.getTargetCommentCount());
            return test;
        }).toList();

        return new GetMyPostsResponse(postCount, getMyPostResponse);
    }

    @Transactional
    public PostsReadResponse read(Long userId, Long postId) {
        if (userId != null) {
            PostsQueryModel model = getPostsWithLikeStatusToQueryModel(userId, postId);

            List<Comment> comments = commentRepository.findAllByPostsId(postId);
            List<CommentResponse> commentResponse = comments.stream().map(CommentResponse::from).toList();

            viewService.increase(postId, userId);
            return PostsReadResponse.from(model, commentResponse);
        }

        Posts post = postsRepository.findById(postId)
                .orElseThrow(PostsNotFoundException::new);
        PostsQueryModel model = PostsQueryModel.create(post, false);

        List<Comment> comments = commentRepository.findAllByPostsId(postId);
        List<CommentResponse> commentResponse = comments.stream().map(CommentResponse::from).toList();

        return PostsReadResponse.from(model, commentResponse);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollIng(Long pageSize, Long lastPostId) {
        return readAllInfiniteScrollPostsIds(lastPostId, pageSize);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollDone(Long pageSize, Long lastPostId) {
        return readAllInfiniteScrollPostsIdsDone(lastPostId, pageSize);
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

    public GetLikedPostsWithSizeResponse getLikedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsLikedByUserId(userId);
        long postsCount = posts.size();

        List<GetLikedPostsResponse> getLikedPostsResponses = posts.stream().map(GetLikedPostsResponse::from).toList();
        return new GetLikedPostsWithSizeResponse(postsCount, getLikedPostsResponses);
    }

    public GetCommentedPostsWithSizeResponse getCommentedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsCommentedByUserId(userId);
        long postsSize = posts.size();

        List<GetCommentedPostsResponse> getCommentedPostsResponses = posts.stream().map(GetCommentedPostsResponse::from)
                .toList();
        return new GetCommentedPostsWithSizeResponse(postsSize, getCommentedPostsResponses);
    }

    private void createPostsCommentCount(CreatePostRequest createPostRequest, Posts posts) {
        PostsCommentCount postsCommentCount = PostsCommentCount.of(posts.getId(),
                createPostRequest.commentCountLimit());
        postsCommentCountRepository.save(postsCommentCount);
    }

    private PostsQueryModel getPostsWithLikeStatusToQueryModel(Long userId, Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(PostsNotFoundException::new);
        boolean isLiked = likeRepository.existsByPostIdAndUserId(postId, userId);
        return PostsQueryModel.create(post, isLiked);
    }

    private void addRedisRandomPool(Posts savedPost) {
        postsRandomShakeRepository.addCandidate(new ShowRandomStoryOnShakeResponse(
                savedPost.getId(),
                savedPost.getUserId(),
                savedPost.getTitle(),
                savedPost.getContent()
        ));
    }

    private static void validateInvalidAutoCompletion(CreatePostRequest createPostRequest) {
        if (createPostRequest.completionType().equals(CompletionType.AUTO_COMPLETION)
                && createPostRequest.commentCountLimit() == null) {
            throw new IllegalArgumentException(
                    "Comment count limit must not be null when the post is set to auto-complete.");
        }
    }

    private void createPostCommentCount(Posts savedPost, Long commentCountLimit) {
        CommentCount commentCount = CommentCount.of(savedPost.getId(), 0L,
                commentCountLimit == null ? 0L : commentCountLimit);
        commentCountRepository.save(commentCount);
    }

    private Optional<PostsQueryModel> fetch(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(PostsNotFoundException::new);
        PostsQueryModel model = PostsQueryModel.create(post, Boolean.FALSE);

        postsQueryModelRepository.create(model, Duration.ofSeconds(60));
        return Optional.of(model);
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

    private List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollPostsIds(Long lastPostId, Long pageSize) {
        if (lastPostId == null) {
            List<Posts> posts = postsRepository.findAllInfiniteScroll(pageSize);

            return posts.stream()
                    .map(post -> PostsReadInfiniteScrollResponse.from(PostsQueryModel.create(post, false)))
                    .toList();
        }

        List<Posts> posts = postsRepository.findAllInfiniteScroll(pageSize, lastPostId);
        return posts.stream()
                .map(post -> PostsReadInfiniteScrollResponse.from(PostsQueryModel.create(post, false)))
                .toList();
    }

    private List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollPostsIdsDone(Long lastPostId, Long pageSize) {
        if (lastPostId == null) {
            List<Posts> posts = postsRepository.findAllInfiniteScrollDone(pageSize);

            return posts.stream()
                    .map(post -> PostsReadInfiniteScrollResponse.from(PostsQueryModel.create(post, false)))
                    .toList();
        }

        List<Posts> posts = postsRepository.findAllInfiniteScrollDone(pageSize, lastPostId);

        return posts.stream()
                .map(post -> PostsReadInfiniteScrollResponse.from(PostsQueryModel.create(post, false)))
                .toList();
    }
}
