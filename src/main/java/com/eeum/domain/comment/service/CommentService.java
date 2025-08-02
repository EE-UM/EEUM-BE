package com.eeum.domain.comment.service;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.global.securitycore.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentCountRepository commentCountRepository;

    @Transactional
    public CommentResponse create(UserPrincipal userPrincipal, CommentCreateRequest request) {
        Comment comment = Comment.of(
                request.content(),
                request.postId(),
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                request.artworkUrl()
        );
        commentRepository.save(comment);

        int result = commentCountRepository.increase(request.postId());
        if (result == 0) {
            commentCountRepository.save(
                    CommentCount.of(request.postId(), 1L)
            );
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
        Optional<Comment> comment = commentRepository.findByIdAndUserId(userId, commentId)
                .filter(not(Comment::getIsDeleted));
        if (comment.isPresent()) {
            comment.get().delete();
            commentCountRepository.decrease(comment.get().getPostId());
        }
    }
}
