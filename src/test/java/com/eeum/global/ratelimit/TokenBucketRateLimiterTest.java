package com.eeum.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

  @Nested
  @DisplayName("tryConsume")
  class TryConsume {

    @Test
    @DisplayName("capacity 값이 소비 임계값 보다 크다면 capacity 만큼은 연속으로 소비할 수 있다")
    void allowsUpToCapacityConsecutively() {
      // given
      FakeClock fakeClock = new FakeClock(0);
      TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 1.0, fakeClock);

      // when // then
      assertThat(rateLimiter.tryConsume()).isTrue();
      assertThat(rateLimiter.tryConsume()).isTrue();
      assertThat(rateLimiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("capacity를 초과해 소비하면 실패한다")
    void rejectsWhenExceedingCapacity() {
      // given
      FakeClock fakeClock = new FakeClock(0);
      TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 1.0, fakeClock);

      // when
      rateLimiter.tryConsume();
      rateLimiter.tryConsume();

      // then
      assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("시간이 지나 토큰이 리필되면 다시 소비할 수 있다")
    void allowsConsumeAfterRefill() {
      // given
      FakeClock fakeClock = new FakeClock(0);
      TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, fakeClock);

      // when
      rateLimiter.tryConsume();
      rateLimiter.tryConsume();

      fakeClock.advanceSeconds(1);

      // then
      assertThat(rateLimiter.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("여러번 토큰을 리필해도 토큰은 capacity를 초과하지 않는다.")
    void refillDoesNotExceedCapacity() {
      // given
      FakeClock fakeClock = new FakeClock(0);
      TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(2, 10.0, fakeClock);

      // when
      fakeClock.advanceSeconds(100);

      // when // then
      assertThat(rateLimiter.tryConsume()).isTrue();
      assertThat(rateLimiter.tryConsume()).isTrue();
      assertThat(rateLimiter.tryConsume()).isFalse();
    }

    @Nested
    @DisplayName("retryAfterSecond")
    class RetryAfterSecond {

      @Test
      @DisplayName("토큰이 남아있으면 0을 반환한다")
      void returnsZeroWhenTokenAvailable() {
        // given
        FakeClock fakeClock = new FakeClock(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, fakeClock);

        // when // then
        assertThat(rateLimiter.retryAfterSecond()).isZero();
      }

      @Test
      @DisplayName("토큰이 없으면 다음 토큰까지 필요한 시간을 올림 계산해 반환한다")
      void returnsCeiledWaitTimeWhenNoTokenAvailable() {
        // given
        FakeClock fakeClock = new FakeClock(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 0.5, fakeClock);
        rateLimiter.tryConsume();

        assertThat(rateLimiter.retryAfterSecond()).isEqualTo(2);
      }
    }
  }
}