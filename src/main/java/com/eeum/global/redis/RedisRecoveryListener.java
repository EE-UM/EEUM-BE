package com.eeum.global.redis;

import com.eeum.domain.block.repository.BlockQueryModelRepository;
import com.eeum.global.config.PostsRandomShakeWarmUp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 장애 중 건너뛴 Redis 쓰기를 복구한다. 랜덤 풀은 MySQL 기준으로 다시 채우고, 차단 캐시는 비워 다음 조회 때 MySQL에서 다시 만들게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRecoveryListener {

    private final PostsRandomShakeWarmUp postsRandomShakeWarmUp;
    private final BlockQueryModelRepository blockQueryModelRepository;

    @Async
    @EventListener
    public void recover(RedisRecoveredEvent event) {
        if (event.features().contains(RedisFeature.SHAKE_POOL)) {
            postsRandomShakeWarmUp.warmUp();
        }
        if (event.features().contains(RedisFeature.BLOCK_CACHE)) {
            blockQueryModelRepository.deleteAll();
        }
        log.info("[RedisRecovery] 복구 처리 완료 - features={}", event.features());
    }
}
