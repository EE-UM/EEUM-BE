package com.eeum.like.repository;

import com.eeum.like.entity.LikeCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeCountRepository extends JpaRepository<LikeCount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LikeCount> findLockedByPostId(Long postId);

    @Query(
            value = "update like_count set like_count = like_count + 1 where post_id = :postId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("postId") Long postId);

    @Query(
            value = "update like_count set like_count = like_count - 1 where post_id = :postId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("postId") Long postId);
}
