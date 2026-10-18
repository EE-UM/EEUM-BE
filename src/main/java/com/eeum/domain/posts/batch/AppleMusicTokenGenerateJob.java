package com.eeum.domain.posts.batch;

import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
import com.eeum.global.batch.BatchJobRecorder;
import com.eeum.global.batch.BatchResult;
import com.eeum.global.infrastructure.applemusickit.AppleMusickit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppleMusicTokenGenerateJob {

    private final DeveloperTokenRepository developerTokenRepository;
    private final AppleMusickit appleMusickit;
    private final BatchJobRecorder batchJobRecorder;

    @Scheduled(cron = "0 0 3 1 1,4,7,10 *", zone = "Asia/Seoul")
    public void runEveryThreeMonth() {
        batchJobRecorder.run("APPLE_MUSIC_TOKEN_GENERATE", () -> {
            String token = appleMusickit.generateToken();
            developerTokenRepository.save(DeveloperToken.of(token));
            log.info("[Batch] AppleMusicToken is regenerated.");
            return BatchResult.of(1, 0);
        });
    }
}
