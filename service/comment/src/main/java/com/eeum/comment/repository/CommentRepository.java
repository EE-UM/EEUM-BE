package com.eeum.comment.repository;

import com.eeum.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Locale;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            value = "select c.* from comments c where c.user_id = :userId and c.id = :commentId and c.is_deleted = false",
            nativeQuery = true
    )
    Optional<Comment> findByIdAndUserId(Long userId, Long commentId);
}
