package com.eeum.comment.service;

import com.eeum.comment.dto.request.CommentCreateRequest;
import com.eeum.comment.dto.response.CommentResponse;
import com.eeum.comment.entity.Comment;
import com.eeum.comment.repository.CommentRepository;
import com.eeum.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static java.util.function.Predicate.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(Long userId, CommentCreateRequest request) {
        Comment comment = Comment.of(
                snowflake.nextId(),
                request.content(),
                request.postId(),
                userId
        );
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    public CommentResponse read(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        commentRepository.findByIdAndUserId(userId, commentId)
                .filter(not(Comment::getIsDeleted))
                .ifPresent(Comment::delete);
    }
}
