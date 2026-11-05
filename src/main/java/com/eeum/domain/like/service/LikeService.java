package com.eeum.domain.like.service;

import com.eeum.domain.like.dto.response.LikeResponse;
import com.eeum.domain.like.entity.Like;
import com.eeum.domain.like.entity.LikeCount;
import com.eeum.domain.like.repository.LikeCountRepository;
import com.eeum.domain.like.repository.LikeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;
  private final LikeCountRepository likeCountRepository;
  private final PostsRepository postsRepository;

  public LikeResponse read(Long postId, Long userId) {
    return likeRepository.findByPostIdAndUserId(postId, userId)
        .map(LikeResponse::from)
        .orElseThrow(() -> new EntityNotFoundException("즐겨찾기 내역이 존재하지 않습니다."));
  }

  /**
   * 중복 좋아요는 likes(post_id, user_id) 유니크 키로 막는다. 동시 요청으로 조회 검사를 통과해도 INSERT 가 유니크 키에 걸려
   * 트랜잭션 전체가 롤백되므로 카운트도 증가하지 않는다.
   */
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void like(Long postId, Long userId) {
    postsRepository.findById(postId)
        .orElseThrow(() -> new NullPointerException("해당 게시글이 존재하지 않습니다."));

    if (likeRepository.findByPostIdAndUserId(postId, userId).isPresent()) {
      throw alreadyLiked();
    }
    try {
      likeRepository.saveAndFlush(Like.of(userId, postId));
    } catch (DataIntegrityViolationException e) {
      throw alreadyLiked();
    }

    int result = likeCountRepository.increase(postId);
    if (result == 0) {
      likeCountRepository.save(LikeCount.of(postId, 1L));
    }
  }

  private static RuntimeException alreadyLiked() {
    return new RuntimeException("이미 좋아요를 누른 상태입니다.");
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void unlike(Long postId, Long userId) {
    likeRepository.findLockedByPostIdAndUserId(postId, userId)
        .ifPresent(like -> {
          likeRepository.delete(like);
          likeCountRepository.decrease(postId);
        });
  }

  public Long count(Long postId) {
    return likeCountRepository.findById(postId)
        .map(LikeCount::getLikeCount)
        .orElse(0L);
  }

  public List<LikeResponse> readUserLikedPosts(Long userId) {
    return likeRepository.findAllByUserId(userId);
  }
}