package com.eeum.view.service;

import com.eeum.view.repository.ViewCountRepository;
import com.eeum.view.repository.ViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewCountRepository viewCountRepository;
    private final ViewCountBackupProcessor viewCountBackupProcessor;
    private final ViewDistributedLockRepository viewDistributedLockRepository;

    private static final int BACK_UP_BACH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long postId, Long userId) {
        if (!viewDistributedLockRepository.lock(postId, userId, TTL)) {
            return viewCountRepository.read(postId);
        }

        Long count = viewCountRepository.increase(postId);
        if (count % BACK_UP_BACH_SIZE == 0) {
            viewCountBackupProcessor.backUp(postId, count);
        }
        return count;
    }

    public Long count(Long postId) {
        return viewCountRepository.read(postId);
    }
}
