package com.eeum.like.service;

import com.eeum.common.snowflake.Snowflake;
import com.eeum.like.dto.response.LikeResponse;
import com.eeum.like.entity.Like;
import com.eeum.like.repository.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class LikeService {

    private final Snowflake snowflake = new Snowflake();
    private final LikeRepository likeRepository;

    public LikeResponse read(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(LikeResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("즐겨찾기 내역이 존재하지 않습니다."));
    }

    @Transactional
    public void like(Long postId, Long userId) {
        likeRepository.save(
                Like.of(
                        snowflake.nextId(),
                        userId,
                        postId
                )
        );
    }

    @Transactional
    public void unlike(Long postId, Long userId) {
        likeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(likeRepository::delete);
    }
}