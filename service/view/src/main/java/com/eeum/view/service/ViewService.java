package com.eeum.view.service;

import com.eeum.view.repository.ViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewCountRepository viewCountRepository;
    private final ViewCountBackupProcessor viewCountBackupProcessor;
    private static final int BACK_UP_BACH_SIZE = 100;

    public Long increase(Long postId, Long userId) {
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
