package com.eeum.domain.posts.service;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.exception.PostsNotFoundException;
import com.eeum.domain.posts.repository.PostsQueryModelRepository;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 게시글 soft delete / 복구의 단일 진입점. 삭제 사유를 기록하고, 커밋 이후에 Redis 캐시(랜덤 풀, 쿼리 모델)를 정리한다.
 */
@Service
@RequiredArgsConstructor
public class PostsDeletionService {

    private final PostsRepository postsRepository;
    private final PostsRandomShakeRepository postsRandomShakeRepository;
    private final PostsQueryModelRepository postsQueryModelRepository;

    /**
     * @return 이번 호출로 삭제되었으면 true, 이미 삭제된 게시글이면 false
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean softDelete(Long postId, Long deletedBy, DeleteReason reason) {
        int updated = postsRepository.softDelete(postId, deletedBy, reason.name(),
            LocalDateTime.now());
        if (updated == 0) {
            return false;
        }

        runAfterCommit(() -> {
            postsRandomShakeRepository.removeCandidate(String.valueOf(postId));
            postsQueryModelRepository.delete(postId);
        });
        return true;
    }

    /**
     * @return 이번 호출로 복구되었으면 true, 삭제 상태가 아니면 false
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean restore(Long postId) {
        int restored = postsRepository.restore(postId);
        if (restored == 0) {
            return false;
        }

        Posts posts = postsRepository.findById(postId)
            .orElseThrow(PostsNotFoundException::new);
        if (!posts.getIsCompleted()) {
            ShowRandomStoryOnShakeResponse candidate = new ShowRandomStoryOnShakeResponse(
                posts.getId(), posts.getUserId(), posts.getTitle(), posts.getContent());
            runAfterCommit(() -> postsRandomShakeRepository.addCandidate(candidate));
        }
        return true;
    }

    private void runAfterCommit(Runnable task) {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
    }
}
