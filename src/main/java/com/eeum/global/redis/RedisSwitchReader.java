package com.eeum.global.redis;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 기능별 Redis 사용 여부를 redis_switch 테이블에서 읽는다. 요청마다 DB를 조회하지 않도록 로컬 캐시에 TTL 동안 보관하므로, 스위치 값을
 * 바꾸면 최대 TTL 뒤에 각 서버에 반영된다. Redis 장애 중에도 읽을 수 있어야 하므로 Redis가 아닌 로컬 캐시를 쓴다.
 */
@Slf4j
@Component
public class RedisSwitchReader {

    private static final Duration TTL = Duration.ofSeconds(10);

    private final RedisSwitchRepository redisSwitchRepository;
    private final LoadingCache<RedisFeature, Boolean> cache;

    public RedisSwitchReader(RedisSwitchRepository redisSwitchRepository) {
        this.redisSwitchRepository = redisSwitchRepository;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(TTL)
            .build(this::load);
    }

    public boolean isEnabled(RedisFeature feature) {
        return cache.get(feature);
    }

    private Boolean load(RedisFeature feature) {
        try {
            return redisSwitchRepository.findById(feature)
                .map(RedisSwitch::isEnabled)
                .orElse(true);
        } catch (RuntimeException e) {
            log.warn("[RedisSwitch] 스위치 조회 실패, Redis 사용으로 간주 - feature={}", feature, e);
            return true;
        }
    }
}
