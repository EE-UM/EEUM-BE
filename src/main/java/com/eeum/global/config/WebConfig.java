package com.eeum.global.config;

import com.eeum.global.ratelimit.TokenBucketRateLimiter;
import com.eeum.global.ratelimit.inbound.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final TokenBucketRateLimiter guestLoginLimiter;
  private final TokenBucketRateLimiter appleMusicSearchLimiter;

  public WebConfig(
      @Value("${rate-limit.guest-login.capacity}") long guestCapacity,
      @Value("${rate-limit.guest-login.refill-per-second}") double guestRefillPerSecond,
      @Value("${rate-limit.apple-music-search.capacity}") long searchCapacity,
      @Value("${rate-limit.apple-music-search.refill-per-second}") double searchRefillPerSecond
  ) {
    this.guestLoginLimiter = new TokenBucketRateLimiter(guestCapacity, guestRefillPerSecond,
        System::nanoTime);
    this.appleMusicSearchLimiter = new TokenBucketRateLimiter(searchCapacity, searchRefillPerSecond,
        System::nanoTime);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RateLimitInterceptor(guestLoginLimiter))
        .addPathPatterns("/user/guest");
    registry.addInterceptor(new RateLimitInterceptor(appleMusicSearchLimiter))
        .addPathPatterns("/apple-music/search");
  }
}
