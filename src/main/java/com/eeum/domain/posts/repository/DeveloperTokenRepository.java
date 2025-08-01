package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.entity.DeveloperToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeveloperTokenRepository extends JpaRepository<DeveloperToken, Long> {
    Optional<DeveloperToken> findTopByOrderByCreatedAtDesc();
}
