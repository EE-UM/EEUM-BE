package com.eeum.domain.view.service;

import com.eeum.domain.view.repository.PostViewLockRepository;
import com.eeum.domain.view.repository.ViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ViewService {
    private final ViewCountRepository viewCountRepository;
    private final PostViewCountBackUpProcessor postViewCountBackUpProcessor;
    private final PostViewLockRepository postViewLockRepository;

    private static final int BACK_UP_BACH_SIZE = 10;
    private static final Duration TTL = Duration.ofSeconds(3);

    public Long increase(Long postId, Long userId) {
        if (!postViewLockRepository.lock(postId, userId, TTL)) {
            return viewCountRepository.read(postId);
        }

        Long count = viewCountRepository.increase(postId);
        if (count % BACK_UP_BACH_SIZE == 0) {
            postViewCountBackUpProcessor.backup(postId, count);
        }
        return count;
    }

    public Long count(Long postId) {
        return viewCountRepository.read(postId);
    }
}
