package com.eeum.domain.posts.service;

import com.eeum.domain.block.service.BlockService;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.like.repository.LikeRepository;
import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import com.eeum.domain.notification.publisher.SpamFilterPublisher;
import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.dto.request.UpdatePostRequest;
import com.eeum.domain.posts.dto.response.CompletePostResponse;
import com.eeum.domain.posts.dto.response.CreatePostResponse;
import com.eeum.domain.posts.dto.response.GetCommentedPostsResponse;
import com.eeum.domain.posts.dto.response.GetCommentedPostsWithSizeResponse;
import com.eeum.domain.posts.dto.response.GetLikedPostsResponse;
import com.eeum.domain.posts.dto.response.GetLikedPostsWithSizeResponse;
import com.eeum.domain.posts.dto.response.GetMyPostResponse;
import com.eeum.domain.posts.dto.response.GetMyPostsResponse;
import com.eeum.domain.posts.dto.response.PostsReadInfiniteScrollResponse;
import com.eeum.domain.posts.dto.response.PostsReadResponse;
import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.dto.response.UpdatePostResponse;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.entity.PostsCommentCount;
import com.eeum.domain.posts.exception.NoAvailablePostsException;
import com.eeum.domain.posts.exception.PostsNotFoundException;
import com.eeum.domain.posts.repository.PostsCommentCountRepository;
import com.eeum.domain.posts.repository.PostsQueryModel;
import com.eeum.domain.posts.repository.PostsQueryModelRepository;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.domain.view.service.ViewService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostsService {

    public static final int MAX_SHAKE_ATTEMPTS = 3;
    private final PostsRepository postsRepository;
    private final PostsQueryModelRepository postsQueryModelRepository;
    private final CommentRepository commentRepository;
    private final CommentCountRepository commentCountRepository;
    private final LikeRepository likeRepository;
    private final ViewService viewService;
    private final PostsRandomShakeRepository postsRandomShakeRepository;
    private final PostsCommentCountRepository postsCommentCountRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;

    private final SpamFilterPublisher spamFilterPublisher;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        validateInvalidAutoCompletion(createPostRequest);

        Album album = Album.of(createPostRequest.albumName(), createPostRequest.songName(),
            createPostRequest.artistName(), createPostRequest.artworkUrl(),
            createPostRequest.appleMusicUrl());
        Posts posts = Posts.of(createPostRequest.title(), createPostRequest.content(), album,
            userId);
        posts.updateCompletionType(createPostRequest.completionType());
        Posts savedPost = postsRepository.save(posts);

        createPostCommentCount(savedPost, createPostRequest.commentCountLimit());

        addRedisRandomPool(savedPost);

        spamFilterPublisher.publish(SpamFilterRequest.of(posts.getId(), posts.getContent()));

        createPostsCommentCount(createPostRequest, posts);

        return CreatePostResponse.of(posts.getId(), userId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UpdatePostResponse updatePost(Long userId, UpdatePostRequest updatePostRequest) {
        Posts posts = postsRepository.findByIdAndUserId(updatePostRequest.postId(), userId)
            .orElseThrow(() -> new EntityNotFoundException("Can't find the post."));

        Album album = Album.of(updatePostRequest.albumName(), updatePostRequest.songName(),
            updatePostRequest.artistName(),
            updatePostRequest.artworkUrl(), updatePostRequest.appleMusicUrl());

        posts.update(updatePostRequest.title(), updatePostRequest.content(), album);

        if (!posts.getIsCompleted()) {
            addRedisRandomPool(posts);
        } else {
            postsRandomShakeRepository.removeCandidate(String.valueOf(posts.getId()));
        }

        return UpdatePostResponse.from(posts);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long delete(Long userId, Long postId) {
        Posts posts = postsRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Can not find the post."));
        if (!Objects.equals(posts.getUserId(), userId)) {
            throw new IllegalArgumentException("Only the author can delete this post.");
        }

        postsRepository.deleteById(postId);
        postsRandomShakeRepository.removeCandidate(String.valueOf(postId));
        return postId;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShowRandomStoryOnShakeResponse showRandomStoryOnShake(Long userId) {
        Set<Long> blockedUserIds = blockService.getBlockedUserIds(userId);

        ShowRandomStoryOnShakeResponse showRandomStoryOnShakeResponse = postsRandomShakeRepository.pickRandomExcludingInOneShot(
                blockedUserIds,
                MAX_SHAKE_ATTEMPTS)
            .orElseGet(() -> pickRandomFromDatabase(blockedUserIds));
        return showRandomStoryOnShakeResponse;
    }

    public GetMyPostsResponse getMyPosts(Long userId) {
        List<Posts> posts = postsRepository.findByUserId(userId);

        Long postCount = (long) posts.size();
        List<GetMyPostResponse> getMyPostResponse = posts.stream().map(post -> {
            PostsCommentCount postsCommentCount = postsCommentCountRepository.findByPostId(
                    post.getId())
                .orElseThrow(IllegalArgumentException::new);
            GetMyPostResponse test = new GetMyPostResponse(post.getId(), post.getTitle(),
                post.getAlbum().getArtworkUrl(), post.getIsCompleted(),
                postsCommentCount.getCurrentCommentCount(),
                postsCommentCount.getTargetCommentCount());
            return test;
        }).toList();

        return new GetMyPostsResponse(postCount, getMyPostResponse);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostsReadResponse read(Long userId, Long postId) {
        if (userId != null) {
            PostsQueryModel model = getPostsWithLikeStatusToQueryModel(userId, postId);

            List<Comment> comments = commentRepository.findAllByPostsId(postId, userId);
            List<CommentResponse> commentResponse = toCommentResponses(comments);

            viewService.increase(postId, userId);
            return PostsReadResponse.from(model, commentResponse);
        }

        Posts post = postsRepository.findById(postId)
            .orElseThrow(PostsNotFoundException::new);
        String nickname = resolveNickname(post.getUserId());
        PostsQueryModel model = PostsQueryModel.create(post, false, nickname);

        List<Comment> comments = commentRepository.findAllByPostsId(postId, null);
        List<CommentResponse> commentResponse = toCommentResponses(comments);

        return PostsReadResponse.from(model, commentResponse);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollIng(Long pageSize,
        Long lastPostId, Long currentUserId) {
        return readAllInfiniteScrollPostsIds(lastPostId, pageSize, currentUserId);
    }

    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollDone(Long pageSize,
        Long lastPostId, Long currentUserId) {
        return readAllInfiniteScrollPostsIdsDone(lastPostId, pageSize, currentUserId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletePostResponse completePost(Long userId, Long postId) {
        log.info("userId = {}", userId);
        log.info("postId = {}", postId);
        Posts posts = postsRepository.findByIdAndUserId(postId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Can't find a post written by the userId."));

        posts.updateIsCompleted();

        return CompletePostResponse.of(posts.getId(), posts.getUserId(), posts.getIsCompleted());
    }

    public GetLikedPostsWithSizeResponse getLikedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsLikedByUserId(userId);
        long postsCount = posts.size();
        log.info("posts Size: {}", posts.size());

        List<GetLikedPostsResponse> getLikedPostsResponses = posts.stream()
            .map(GetLikedPostsResponse::from).toList();
        return new GetLikedPostsWithSizeResponse(postsCount, getLikedPostsResponses);
    }

    public GetCommentedPostsWithSizeResponse getCommentedPosts(Long userId) {
        List<Posts> posts = postsRepository.findPostsCommentedByUserId(userId);
        long postsSize = posts.size();

        List<GetCommentedPostsResponse> getCommentedPostsResponses = posts.stream()
            .map(GetCommentedPostsResponse::from)
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
        String nickname = resolveNickname(post.getUserId());
        return PostsQueryModel.create(post, isLiked, nickname);
    }

    private String resolveNickname(Long userId) {
        return userRepository.findById(userId).map(User::getNickname).orElse(null);
    }

    private Map<Long, String> resolveNicknames(List<Long> userIds) {
        List<Long> distinctUserIds = userIds.stream().distinct().toList();
        return userRepository.findAllById(distinctUserIds).stream()
            .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    private List<CommentResponse> toCommentResponses(List<Comment> comments) {
        Map<Long, String> nicknameByUserId = resolveNicknames(
            comments.stream().map(Comment::getUserId).toList());
        return comments.stream()
            .map(
                comment -> CommentResponse.from(comment, nicknameByUserId.get(comment.getUserId())))
            .toList();
    }

    private List<PostsReadInfiniteScrollResponse> toInfiniteScrollResponses(List<Posts> posts) {
        Map<Long, String> nicknameByUserId = resolveNicknames(
            posts.stream().map(Posts::getUserId).toList());
        return posts.stream()
            .map(post -> PostsReadInfiniteScrollResponse.from(
                PostsQueryModel.create(post, false, nicknameByUserId.get(post.getUserId()))))
            .toList();
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
        String nickname = resolveNickname(post.getUserId());
        PostsQueryModel model = PostsQueryModel.create(post, Boolean.FALSE, nickname);

        postsQueryModelRepository.create(model, Duration.ofSeconds(60));
        return Optional.of(model);
    }

    private List<PostsReadInfiniteScrollResponse> readAll(List<Long> postsIds) {
        log.info("[PostsReadService.readAll] input postIds: {}", postsIds);
        Map<Long, PostsQueryModel> postsQueryModelMap = postsQueryModelRepository.readAll(postsIds);
        log.info("[PostsReadService.readAll] cached postsMap keys: {}",
            postsQueryModelMap.keySet());

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
                    PostsReadInfiniteScrollResponse dto = PostsReadInfiniteScrollResponse.from(
                        postsQueryModel);
                    log.info("[readAll] successfully converted DTO for postId={}",
                        postsQueryModel.getPostId());
                    return dto;
                } catch (Exception e) {
                    log.error("[readAll] failed to convert DTO for postId={}",
                        postsQueryModel.getPostId(),
                        e);
                    return null;
                }
            })
            .toList();

        log.info("[PostsReadService.readAll] final response size: {}", result.size());
        return result;
    }

    private List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollPostsIds(Long lastPostId,
        Long pageSize, Long userId) {
        if (lastPostId == null) {
            List<Posts> posts = postsRepository.findAllInfiniteScroll(pageSize, userId);
            return toInfiniteScrollResponses(posts);
        }

        List<Posts> posts = postsRepository.findAllInfiniteScroll(pageSize, lastPostId, userId);
        return toInfiniteScrollResponses(posts);
    }

    private List<PostsReadInfiniteScrollResponse> readAllInfiniteScrollPostsIdsDone(Long lastPostId,
        Long pageSize, Long userId) {
        if (lastPostId == null) {
            List<Posts> posts = postsRepository.findAllInfiniteScrollDone(pageSize, userId);
            return toInfiniteScrollResponses(posts);
        }

        List<Posts> posts = postsRepository.findAllInfiniteScrollDone(pageSize, lastPostId, userId);
        return toInfiniteScrollResponses(posts);
    }

    private ShowRandomStoryOnShakeResponse pickRandomFromDatabase(Set<Long> blockedUserIds) {
        Collection<Long> excludedIds = blockedUserIds.isEmpty()
            ? List.of(0L)
            : blockedUserIds;

        return postsRepository.findRandomActivePostExcludingUserIds(excludedIds)
            .map(post -> new ShowRandomStoryOnShakeResponse(
                post.getId(), post.getUserId(), post.getTitle(), post.getContent()
            )).orElseThrow(NoAvailablePostsException::new);
    }
}
