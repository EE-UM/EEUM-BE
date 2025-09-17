package com.eeum.domain.comment.repository;

import com.eeum.domain.comment.entity.CommentCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentCountRepository extends JpaRepository<CommentCount, Long> {

    @Query(
            value = "update comment_count set comment_count = comment_count + 1 where post_id = :postId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("postId") Long postId);

    @Query(
            value = "update comment_count set comment_count = comment_count - 1 where post_id = :postId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("postId") Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CommentCount> findLockedByPostId(Long postId);

    Optional<CommentCount> findByPostId(Long aLong);
}
