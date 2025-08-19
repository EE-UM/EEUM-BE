package com.eeum.domain.view.service;

import com.eeum.domain.view.entity.PostViewCount;
import com.eeum.domain.view.repository.ViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostViewCountBackUpProcessor {
    private final ViewCountBackUpRepository viewCountBackUpRepository;

    @Transactional
    public void backup(Long postId, Long viewCount) {
        int result = viewCountBackUpRepository.updateViewCount(postId, viewCount);
        if (result == 0) {
            viewCountBackUpRepository.findById(postId)
                    .ifPresentOrElse(ignored -> {
                            },
                            () -> viewCountBackUpRepository.save(PostViewCount.init(postId, viewCount))
                    );
        }
    }

}
