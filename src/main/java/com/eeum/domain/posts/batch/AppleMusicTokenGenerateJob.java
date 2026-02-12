package com.eeum.domain.posts.batch;

import com.eeum.domain.posts.entity.DeveloperToken;
import com.eeum.domain.posts.repository.DeveloperTokenRepository;
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

    @Scheduled(cron = "0 0 3 1 1,4,7,10 *", zone = "Asia/Seoul")
    public void runEveryThreeMonth() {
        String token = appleMusickit.generateToken();
        DeveloperToken developerToken = DeveloperToken.of(token);
        developerTokenRepository.save(developerToken);

        log.info("[Batch] AppleMusicToken is regenerated.");
    }
}
