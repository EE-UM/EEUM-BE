package com.eeum.global.redis;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * Redis 호출을 감싸 스위치가 꺼져 있거나 연속 실패로 우회 중이면 Redis를 건너뛰고 fallback 을 실행한다. Redis 오류(DataAccessException)는
 * 실패로 기록한 뒤 fallback 으로 응답하고, 그 외 예외는 그대로 던진다.
 * <p>
 * 장애 중에는 Redis 쓰기가 누락되므로, Redis가 다시 응답하거나 꺼졌던 스위치가 켜지면 {@link RedisRecoveredEvent}를 발행해 데이터를 다시 맞춘다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisGuard {

    private final RedisSwitchReader redisSwitchReader;
    private final RedisFailureDetector redisFailureDetector;
    private final ApplicationEventPublisher eventPublisher;

    private final Set<RedisFeature> switchedOff = ConcurrentHashMap.newKeySet();

    public <T> T call(RedisFeature feature, Supplier<T> redisCall, Supplier<T> fallback) {
        if (!isSwitchOn(feature) || !redisFailureDetector.allowRequest()) {
            return fallback.get();
        }
        try {
            T result = redisCall.get();
            if (redisFailureDetector.recordSuccess()) {
                log.warn("[RedisGuard] Redis 복구 - Redis 경로로 복귀");
                eventPublisher.publishEvent(new RedisRecoveredEvent(EnumSet.allOf(RedisFeature.class)));
            }
            return result;
        } catch (DataAccessException e) {
            if (redisFailureDetector.recordFailure()) {
                log.error("[RedisGuard] Redis 연속 실패 - MySQL 대체 경로로 전환", e);
            } else {
                log.warn("[RedisGuard] Redis 호출 실패 - feature={}, message={}", feature,
                    e.getMessage());
            }
            return fallback.get();
        }
    }

    public void run(RedisFeature feature, Runnable redisCall) {
        call(feature, () -> {
            redisCall.run();
            return null;
        }, () -> null);
    }

    private boolean isSwitchOn(RedisFeature feature) {
        if (!redisSwitchReader.isEnabled(feature)) {
            if (switchedOff.add(feature)) {
                log.warn("[RedisGuard] 스위치 OFF - feature={}", feature);
            }
            return false;
        }
        if (switchedOff.remove(feature)) {
            log.warn("[RedisGuard] 스위치 ON - feature={}", feature);
            eventPublisher.publishEvent(new RedisRecoveredEvent(EnumSet.of(feature)));
        }
        return true;
    }
}
