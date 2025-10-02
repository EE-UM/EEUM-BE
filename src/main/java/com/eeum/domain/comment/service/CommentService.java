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
import com.eeum.global.securitycore.token.UserPrincipal;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_RETRIES = 3;

    private final CommentRepository commentRepository;
    private final CommentCountRepository commentCountRepository;
    private final PostsRepository postsRepository;

    private final CommentProducer commentProducer;

    @Transactional
    public CommentResponse create(UserPrincipal userPrincipal, CommentCreateRequest request) {
        CommentCount commentCount = commentCountRepository.findByPostId(request.postId()).orElseThrow(() -> new IllegalArgumentException("Can't find CommentCount Entity."));
        Posts postForValidate = postsRepository.findById(request.postId()).orElseThrow(() -> new IllegalArgumentException("Can't find Post Entity."));
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
        return CommentResponse.from(comment);
    }

    public List<CommentResponse> readAllCommentsOfPost(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostsId(postId);
        List<CommentResponse> commentResponseList = comments.stream().map(CommentResponse::from).toList();
        return commentResponseList;
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        commentRepository.findByIdAndUserId(userId, commentId)
                .filter(not(Comment::getIsDeleted))
                .ifPresent(comment -> {
                    commentRepository.delete(comment);
                    CommentCount commentCount = commentCountRepository.findById(comment.getPostId())
                            .orElseThrow(() -> new IllegalArgumentException("Can not find CommentCount Entity."));
                    commentCount.decreaseSafely();
                });
    }

    private static void validateDuplicateMusic(CommentCreateRequest request, Posts postForValidate) {
        if (postForValidate.getAlbum().getAlbumName().equals(request.albumName()) &&
                postForValidate.getAlbum().getArtistName().equals(request.artistName())) {
            throw new DuplicateMusicException("The music used in the comment cannot be the same as the music used in the post.");
        }
    }

    private static Comment createComment(UserPrincipal userPrincipal, CommentCreateRequest request) {
        Album album = Album.of(request.albumName(), request.songName(), request.artistName(), request.artworkUrl(), request.appleMusicUrl());
        return Comment.of(
                request.content(),
                request.postId(),
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                album
        );
    }

    private static void validatePostAvailableStatus(CommentCount commentCount, Posts postForValidate) {
        if (commentCount.getCommentCount() >= commentCount.getCommentCountLimit()) {
            throw new AlreadyFinishedPostException("comment_limit_reached.");
        }
        if (postForValidate.getIsCompleted()) {
            throw new AlreadyFinishedPostException("post_completed.");
        }
    }
}
