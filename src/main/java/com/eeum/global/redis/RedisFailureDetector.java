package com.eeum.global.redis;

import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * Redis 호출 실패를 감지해 연속 실패 시 일정 시간 Redis를 건너뛰게 한다. Redis 인스턴스가 하나라 기능별이 아닌 서버 전체에서 하나만 둔다.
 * <ul>
 *   <li>NORMAL: 모든 호출 허용. 연속 실패가 threshold 에 도달하면 BYPASS.</li>
 *   <li>BYPASS: bypassDuration 동안 호출을 건너뛴다. 지나면 PROBING 으로 바꾸고 한 요청만 시도한다.</li>
 *   <li>PROBING: 시도한 요청이 성공하면 NORMAL(복구), 실패하면 다시 BYPASS. 시도 결과가 기록되지 않은 채
 *   bypassDuration 이 지나면 다른 요청이 다시 시도한다.</li>
 * </ul>
 */
public class RedisFailureDetector {

    public enum State {NORMAL, BYPASS, PROBING}

    private final int failureThreshold;
    private final long bypassNanos;
    private final LongSupplier nanoClock;

    private State state = State.NORMAL;
    private int consecutiveFailures;
    private long stateChangedAt;

    public RedisFailureDetector(int failureThreshold, Duration bypassDuration,
        LongSupplier nanoClock) {
        this.failureThreshold = failureThreshold;
        this.bypassNanos = bypassDuration.toNanos();
        this.nanoClock = nanoClock;
    }

    public synchronized boolean allowRequest() {
        if (state == State.NORMAL) {
            return true;
        }
        long now = nanoClock.getAsLong();
        if (now - stateChangedAt < bypassNanos) {
            return false;
        }
        state = State.PROBING;
        stateChangedAt = now;
        return true;
    }

    /**
     * @return PROBING 에서 NORMAL 로 복구되었으면 true
     */
    public synchronized boolean recordSuccess() {
        boolean recovered = state != State.NORMAL;
        state = State.NORMAL;
        consecutiveFailures = 0;
        return recovered;
    }

    /**
     * @return 이번 실패로 BYPASS 이 되었으면 true
     */
    public synchronized boolean recordFailure() {
        if (state == State.NORMAL && ++consecutiveFailures < failureThreshold) {
            return false;
        }
        boolean bypassed = state == State.NORMAL;
        state = State.BYPASS;
        stateChangedAt = nanoClock.getAsLong();
        return bypassed;
    }

    public synchronized State state() {
        return state;
    }
}
