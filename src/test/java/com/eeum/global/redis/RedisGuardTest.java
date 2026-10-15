package com.eeum.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisConnectionFailureException;

@ExtendWith(MockitoExtension.class)
class RedisGuardTest {

    private static final int THRESHOLD = 2;
    private static final Duration BYPASS_DURATION = Duration.ofSeconds(10);

    @Mock
    private RedisSwitchReader redisSwitchReader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final AtomicLong nanos = new AtomicLong();
    private RedisGuard redisGuard;

    @BeforeEach
    void setUp() {
        RedisFailureDetector detector = new RedisFailureDetector(THRESHOLD, BYPASS_DURATION,
            nanos::get);
        redisGuard = new RedisGuard(redisSwitchReader, detector, eventPublisher);
    }

    private String failingRedisCall() {
        throw new RedisConnectionFailureException("down");
    }

    @Test
    @DisplayName("스위치가 켜져 있고 Redis가 정상이면 Redis 결과를 반환한다.")
    void returnsRedisResult() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(true);

        String result = redisGuard.call(RedisFeature.SHAKE_POOL, () -> "redis", () -> "mysql");

        assertThat(result).isEqualTo("redis");
    }

    @Test
    @DisplayName("Redis 호출이 실패하면 fallback 결과를 반환한다.")
    void returnsFallbackOnRedisFailure() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(true);

        String result = redisGuard.call(RedisFeature.SHAKE_POOL, this::failingRedisCall,
            () -> "mysql");

        assertThat(result).isEqualTo("mysql");
    }

    @Test
    @DisplayName("Redis 오류가 아닌 예외는 fallback 하지 않고 그대로 던진다.")
    void rethrowsNonRedisException() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(true);

        assertThatThrownBy(() -> redisGuard.call(RedisFeature.SHAKE_POOL, () -> {
            throw new IllegalStateException("bug");
        }, () -> "mysql")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("스위치가 꺼져 있으면 Redis를 호출하지 않는다.")
    void skipsRedisWhenSwitchOff() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(false);
        AtomicInteger redisCalls = new AtomicInteger();

        String result = redisGuard.call(RedisFeature.SHAKE_POOL, () -> {
            redisCalls.incrementAndGet();
            return "redis";
        }, () -> "mysql");

        assertThat(result).isEqualTo("mysql");
        assertThat(redisCalls).hasValue(0);
    }

    @Test
    @DisplayName("연속 실패로 우회 중이면 Redis를 호출하지 않는다.")
    void skipsRedisWhileBypassing() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(true);
        for (int i = 0; i < THRESHOLD; i++) {
            redisGuard.call(RedisFeature.SHAKE_POOL, this::failingRedisCall, () -> "mysql");
        }
        AtomicInteger redisCalls = new AtomicInteger();

        String result = redisGuard.call(RedisFeature.SHAKE_POOL, () -> {
            redisCalls.incrementAndGet();
            return "redis";
        }, () -> "mysql");

        assertThat(result).isEqualTo("mysql");
        assertThat(redisCalls).hasValue(0);
    }

    @Test
    @DisplayName("우회 중 시도가 성공하면 모든 기능의 복구 이벤트를 발행한다.")
    void publishesRecoveryWhenRedisResponds() {
        given(redisSwitchReader.isEnabled(RedisFeature.SHAKE_POOL)).willReturn(true);
        for (int i = 0; i < THRESHOLD; i++) {
            redisGuard.call(RedisFeature.SHAKE_POOL, this::failingRedisCall, () -> "mysql");
        }
        nanos.addAndGet(BYPASS_DURATION.toNanos());

        String result = redisGuard.call(RedisFeature.SHAKE_POOL, () -> "redis", () -> "mysql");

        assertThat(result).isEqualTo("redis");
        verify(eventPublisher).publishEvent(
            new RedisRecoveredEvent(EnumSet.allOf(RedisFeature.class)));
    }

    @Test
    @DisplayName("꺼졌던 스위치가 다시 켜지면 해당 기능의 복구 이벤트를 발행한다.")
    void publishesRecoveryWhenSwitchTurnsOn() {
        given(redisSwitchReader.isEnabled(RedisFeature.BLOCK_CACHE)).willReturn(false, true);

        redisGuard.run(RedisFeature.BLOCK_CACHE, () -> {
        });
        redisGuard.run(RedisFeature.BLOCK_CACHE, () -> {
        });

        verify(eventPublisher).publishEvent(
            new RedisRecoveredEvent(EnumSet.of(RedisFeature.BLOCK_CACHE)));
    }

    @Test
    @DisplayName("스위치가 계속 켜져 있으면 복구 이벤트를 발행하지 않는다.")
    void doesNotPublishWhenSwitchStaysOn() {
        given(redisSwitchReader.isEnabled(RedisFeature.BLOCK_CACHE)).willReturn(true);

        redisGuard.run(RedisFeature.BLOCK_CACHE, () -> {
        });

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }
}
