package com.eeum.domain.comment.service;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Album;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import com.eeum.domain.comment.exception.DuplicateMusicException;
import com.eeum.domain.comment.producer.CommentProducer;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.token.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

  private static final int MAX_RETRIES = 3;

  private final CommentRepository commentRepository;
  private final CommentCountRepository commentCountRepository;
  private final PostsRepository postsRepository;
  private final UserRepository userRepository;

  private final CommentProducer commentProducer;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public CommentResponse create(UserPrincipal userPrincipal, CommentCreateRequest request) {
    CommentCount commentCount = commentCountRepository.findByPostId(request.postId())
        .orElseThrow(() -> new IllegalArgumentException("Can't find CommentCount Entity."));
    Posts postForValidate = postsRepository.findById(request.postId())
        .orElseThrow(() -> new IllegalArgumentException("Can't find Post Entity."));
    validatePostAvailableStatus(commentCount, postForValidate);
    validateDuplicateMusic(request, postForValidate);
    Comment comment = createComment(userPrincipal, request);
    commentRepository.save(comment);
    commentCount.increaseOrThrow();

    if (commentCount.hitLimit()) {
      postForValidate.updateIsCompleted();
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          commentProducer.sendCompletedPost(postForValidate.getId());
        }
      });
    }
    String nickname = userRepository.findById(userPrincipal.getId())
        .map(User::getNickname)
        .orElse(null);
    return CommentResponse.from(comment, nickname);
  }

  public List<CommentResponse> readAllCommentsOfPost(Long postId, Long currentUserId) {
    List<Comment> comments = commentRepository.findAllByPostsId(postId, currentUserId);
    Map<Long, String> nicknameByUserId = resolveNicknames(
        comments.stream().map(Comment::getUserId).toList());

    List<CommentResponse> commentResponseList = comments.stream()
        .map(comment -> CommentResponse.from(comment, nicknameByUserId.get(comment.getUserId())))
        .toList();
    return commentResponseList;
  }

  private Map<Long, String> resolveNicknames(List<Long> userIds) {
    List<Long> distinctUserIds = userIds.stream().distinct().toList();
    return userRepository.findAllById(distinctUserIds).stream()
        .collect(Collectors.toMap(User::getId, User::getNickname));
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void delete(Long userId, Long commentId) {
    commentRepository.findByIdAndUserId(userId, commentId).ifPresent(comment -> {
      commentRepository.softDelete(commentId);

      CommentCount commentCount = commentCountRepository.findById(comment.getPostId())
          .orElseThrow(() -> new IllegalArgumentException("Can not find CommentCount Entity."));

      commentCount.decreaseSafely();
    });
  }

  private static void validateDuplicateMusic(CommentCreateRequest request, Posts postForValidate) {
    if (postForValidate.getAlbum().getAlbumName().equals(request.albumName())
        && postForValidate.getAlbum().getArtistName().equals(request.artistName())) {
      throw new DuplicateMusicException(
          "The music used in the comment cannot be the same as the music used in the post.");
    }
  }

  private static Comment createComment(UserPrincipal userPrincipal, CommentCreateRequest request) {
    Album album = Album.of(request.albumName(), request.songName(), request.artistName(),
        request.artworkUrl(), request.appleMusicUrl());

    return Comment.of(request.content(), request.postId(), userPrincipal.getId(),
        userPrincipal.getUsername(), album);
  }

  private static void validatePostAvailableStatus(CommentCount commentCount,
      Posts postForValidate) {
    if (commentCount.getCommentCount() >= commentCount.getCommentCountLimit()) {
      throw new AlreadyFinishedPostException("comment_limit_reached.");
    }
    if (postForValidate.getIsCompleted()) {
      throw new AlreadyFinishedPostException("post_completed.");
    }
  }
}
