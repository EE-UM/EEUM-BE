package com.eeum.like.repository;

import com.eeum.like.entity.Like;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Like> findLockedByPostIdAndUserId(Long postId, Long userId);
}
