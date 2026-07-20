package com.eeum.global.logger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LoggingFilterTest {

  @Nested
  @DisplayName("maskSensitiveData")
  class MaskSensitiveData {

    private final LoggingFilter loggingFilter = new LoggingFilter();

    @Test
    @DisplayName("idToken 값은 마스킹된다")
    void masksIdToken() throws Exception {
      // given
      String body = "{\"idToken\":\"abc.def.ghi\"}";

      // when
      String masked = loggingFilter.maskSensitiveData(body);

      // then
      assertThat(masked).isEqualTo("{\"idToken\":\"***\"}");
    }

    @Test
    @DisplayName("accessToken, refreshToken, password, deviceId 값도 모두 마스킹된다")
    void masksAllSensitiveFields() {
      // given
      String body = "{\"accessToken\":\"a\",\"refreshToken\":\"b\",\"password\":\"c\",\"deviceId\":\"d\"}";

      // when
      String masked = loggingFilter.maskSensitiveData(body);

      // then
      assertThat(masked).isEqualTo(
          "{\"accessToken\":\"***\",\"refreshToken\":\"***\",\"password\":\"***\",\"deviceId\":\"***\"}");
    }
  }
}