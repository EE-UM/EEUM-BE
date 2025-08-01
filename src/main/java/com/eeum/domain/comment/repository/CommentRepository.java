package com.eeum.domain.comment.repository;

import com.eeum.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            value = "select c.* from comments c where c.user_id = :userId and c.id = :commentId and c.is_deleted = false",
            nativeQuery = true
    )
    Optional<Comment> findByIdAndUserId(Long userId, Long commentId);

    @Query(
            value = "select c.* from comments c where c.post_id = :postId order by c.created_at asc",
            nativeQuery = true
    )
    List<Comment> findAllByPostsId(Long postId);
}
