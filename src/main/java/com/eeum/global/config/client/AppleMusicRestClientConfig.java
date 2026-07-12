package com.eeum.global.config.client;

import com.eeum.global.ratelimit.TokenBucketRateLimiter;
import com.eeum.global.ratelimit.outbound.OutboundRateLimitInterceptor;
import com.eeum.global.ratelimit.outbound.RetryAfterInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppleMusicRestClientConfig {

  @Bean
  public RestClient appleMusicClient(
      @Value("${apple-music.base-url}") String baseUrl,
      @Value("${apple-music.connect-timeout-ms}") int connectionTimeoutMs,
      @Value("${apple-music.read-timeout-ms}") int readTimeoutMs,
      @Value("${gateway.max-attempts}") int maxAttempts,
      @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
      @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
  ) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectionTimeoutMs);
    factory.setReadTimeout(readTimeoutMs);

    TokenBucketRateLimiter outboundLimiter = new TokenBucketRateLimiter(outboundCapacity,
        outboundRefillPerSecond, System::nanoTime);

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(factory)
        .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
        .requestInterceptor(new RetryAfterInterceptor(outboundLimiter, maxAttempts))
        .build();
  }
}
