package com.eeum.global.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.eeum.global.redis.RedisFailureDetector.State;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisFailureDetectorTest {

    private static final int THRESHOLD = 3;
    private static final Duration BYPASS_DURATION = Duration.ofSeconds(10);

    private final AtomicLong nanos = new AtomicLong();
    private RedisFailureDetector detector;

    @BeforeEach
    void setUp() {
        detector = new RedisFailureDetector(THRESHOLD, BYPASS_DURATION, nanos::get);
    }

    private void advanceSeconds(long seconds) {
        nanos.addAndGet(Duration.ofSeconds(seconds).toNanos());
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            detector.recordFailure();
        }
    }

    @Test
    @DisplayName("연속 실패가 임계치 미만이면 NORMAL를 유지한다.")
    void staysNormalBelowThreshold() {
        failTimes(THRESHOLD - 1);

        assertThat(detector.state()).isEqualTo(State.NORMAL);
        assertThat(detector.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("중간에 성공하면 연속 실패 횟수가 초기화된다.")
    void successResetsFailureCount() {
        failTimes(THRESHOLD - 1);
        detector.recordSuccess();
        failTimes(THRESHOLD - 1);

        assertThat(detector.state()).isEqualTo(State.NORMAL);
    }

    @Test
    @DisplayName("연속 실패가 임계치에 도달하면 BYPASS이 되어 요청을 건너뛴다.")
    void bypassesAtThreshold() {
        failTimes(THRESHOLD - 1);

        assertThat(detector.recordFailure()).isTrue();
        assertThat(detector.state()).isEqualTo(State.BYPASS);
        assertThat(detector.allowRequest()).isFalse();
    }

    @Test
    @DisplayName("BYPASS 시간이 지나면 한 요청만 시도를 허용한다.")
    void allowsSingleProbeAfterBypassDuration() {
        failTimes(THRESHOLD);
        advanceSeconds(BYPASS_DURATION.toSeconds());

        assertThat(detector.allowRequest()).isTrue();
        assertThat(detector.state()).isEqualTo(State.PROBING);
        assertThat(detector.allowRequest()).isFalse();
    }

    @Test
    @DisplayName("PROBING에서 시도가 성공하면 복구되어 NORMAL가 된다.")
    void returnsToNormalWhenProbeSucceeds() {
        failTimes(THRESHOLD);
        advanceSeconds(BYPASS_DURATION.toSeconds());
        detector.allowRequest();

        assertThat(detector.recordSuccess()).isTrue();
        assertThat(detector.state()).isEqualTo(State.NORMAL);
        assertThat(detector.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("PROBING에서 시도가 실패하면 즉시 다시 BYPASS이 된다.")
    void bypassesAgainWhenProbeFails() {
        failTimes(THRESHOLD);
        advanceSeconds(BYPASS_DURATION.toSeconds());
        detector.allowRequest();

        assertThat(detector.recordFailure()).isFalse();
        assertThat(detector.state()).isEqualTo(State.BYPASS);
        assertThat(detector.allowRequest()).isFalse();
    }

    @Test
    @DisplayName("PROBING 시도 결과가 기록되지 않으면 BYPASS 시간이 지난 뒤 다시 시도를 허용한다.")
    void allowsAnotherProbeWhenProbeResultIsMissing() {
        failTimes(THRESHOLD);
        advanceSeconds(BYPASS_DURATION.toSeconds());
        detector.allowRequest();

        advanceSeconds(BYPASS_DURATION.toSeconds());

        assertThat(detector.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("NORMAL에서 성공은 복구로 보지 않는다.")
    void successInNormalIsNotRecovery() {
        assertThat(detector.recordSuccess()).isFalse();
    }
}
