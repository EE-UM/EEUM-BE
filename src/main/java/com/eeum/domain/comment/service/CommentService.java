package com.eeum.domain.comment.service;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Album;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import com.eeum.domain.comment.exception.DuplicateMusicException;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.global.securitycore.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentCountRepository commentCountRepository;
    private final PostsRepository postsRepository;

    @Transactional
    public CommentResponse create(UserPrincipal userPrincipal, CommentCreateRequest request) {
        CommentCount commentCount = commentCountRepository.findLockedByPostId(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("Can't find CommentCount Entity."));
        Posts postForValidate = postsRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("Can't find Post Entity."));

        validatePostAvailableStatus(commentCount, postForValidate);
        validateDuplicateMusic(request, postForValidate);

        Comment comment = createComment(userPrincipal, request);
        commentRepository.save(comment);

        commentCountRepository.increase(request.postId());

        validateAndUpdatePostCompleteStatus(request, commentCount);

        return CommentResponse.from(comment);
    }

    private static void validateDuplicateMusic(CommentCreateRequest request, Posts postForValidate) {
        if (postForValidate.getAlbum().getAlbumName().equals(request.albumName()) &&
                postForValidate.getAlbum().getArtistName().equals(request.artistName())) {
            throw new DuplicateMusicException("The music used in the comment cannot be the same as the music used in the post.");
        }
    }

    public List<CommentResponse> readAllCommentsOfPost(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostsId(postId);
        List<CommentResponse> commentResponseList = comments.stream().map(CommentResponse::from).toList();
        return commentResponseList;
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Optional<Comment> comment = commentRepository.findByIdAndUserId(userId, commentId)
                .filter(not(Comment::getIsDeleted));
        if (comment.isPresent()) {
            commentRepository.delete(comment.get());

            CommentCount commentCount = commentCountRepository.findLockedByPostId(comment.get().getPostId())
                    .orElseThrow(() -> new RuntimeException("Can't find CommentCount Entity."));
            commentCountRepository.decrease(comment.get().getPostId());
        }
    }

    private void validateAndUpdatePostCompleteStatus(CommentCreateRequest request, CommentCount commentCount) {
        if ((Long.valueOf(commentCount.getCommentCount() + 1)).equals(commentCount.getCommentCountLimit())) {
            Posts posts = postsRepository.findById(request.postId())
                    .orElseThrow(() -> new IllegalArgumentException("That post is not founded."));
            posts.updateIsCompleted();
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
