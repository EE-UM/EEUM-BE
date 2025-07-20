package com.eeum.view.service;

import com.eeum.view.entity.View;
import com.eeum.view.repository.ViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewCountBackupProcessor {

    private final ViewCountBackUpRepository viewCountBackUpRepository;

    @Transactional
    public void backUp(Long postId, Long viewCount) {
        int result = viewCountBackUpRepository.updateViewCount(postId, viewCount);
        if (result == 0) {
            viewCountBackUpRepository.findById(postId)
                    .ifPresentOrElse(ignored -> {
                            },
                            () -> viewCountBackUpRepository.save(View.of(postId, viewCount))
                    );
        }
    }
}
