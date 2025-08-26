package com.eeum.domain.like.repository;

import com.eeum.domain.like.dto.response.LikeResponse;
import com.eeum.domain.like.entity.Like;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Like> findLockedByPostIdAndUserId(Long postId, Long userId);

    @Query(
            value = "select * " +
                    "from like l " +
                    "where l.userId = :userId " +
                    "order by l.created_at desc ",
            nativeQuery = true
    )
    List<LikeResponse> findAllByUserId(@Param("userId") Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
