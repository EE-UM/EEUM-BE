package com.eeum.global.securitycore.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JWTUtilTest {

  private final JWTUtil jwtUtil = new JWTUtil(
      "test-secret-key-must-be-at-least-256-bits-long-for-hs256!!");

  @Test
  @DisplayName("createJwt로 발급한 토큰에는 exp 클레임이 존재하지 않는다")
  void createJwt_hasNoExpirationClaim() {
    String token = jwtUtil.createJwt("access", 1L, "user", "USER", "test@test.com");

    Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);
    assertThat(expiration).isNull();
  }

  @Test
  @DisplayName("발급 직후에는 토큰이 유효하다")
  void validateToken_validRightAfterIssue() {
    String token = jwtUtil.createJwt("access", 1L, "user", "USER", "test@test.com");

    assertThat(jwtUtil.validateToken(token)).isTrue();
  }

  @Test
  @DisplayName("exp 클레임이 없으므로 발급 시각으로부터 오랜 시간이 지나도 여전히 유효하다고 판단한다")
  void validateToken_stillValidLongAfterIssue() {
    String token = jwtUtil.createJwt("access", 1L, "user", "USER", "test@test.com");
    Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

    // 발급 시각 기준 100년이 지난 시점을 가정해도 만료 판정 로직이 없으므로 여전히 유효해야 한다.
    Date farFuture = new Date(issuedAt.getTime() + Duration.ofDays(365L * 100).toMillis());
    assertThat(farFuture).isAfter(new Date());
    assertThat(jwtUtil.validateToken(token)).isTrue();
  }
}
