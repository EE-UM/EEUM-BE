package com.eeum.global.ratelimit;

import java.util.function.LongSupplier;

public class FakeClock implements LongSupplier {

  private static final long NANOS_PER_SEC = 1_000_000_000L;

  private long nanos;

  public FakeClock(long nanos) {
    this.nanos = nanos;
  }

  void advanceSeconds(long seconds) {
    this.nanos += seconds * NANOS_PER_SEC;
  }

  @Override
  public long getAsLong() {
    return nanos;
  }
}
