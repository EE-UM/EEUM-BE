package com.eeum.global.redis;

import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import java.time.Duration;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisResilienceConfig {

    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration BYPASS_DURATION = Duration.ofSeconds(10);
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RedisFailureDetector redisFailureDetector() {
        return new RedisFailureDetector(FAILURE_THRESHOLD, BYPASS_DURATION, System::nanoTime);
    }

    /**
     * 연결이 끊긴 동안 들어온 명령을 재연결될 때까지 큐에 쌓아 두고 커맨드 타임아웃까지 기다리는 대신 즉시 실패시킨다.
     */
    @Bean
    public LettuceClientOptionsBuilderCustomizer rejectCommandsWhenDisconnected() {
        return builder -> builder.disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS);
    }

    /**
     * 연결은 살아 있지만 Redis가 응답하지 않는 경우 Lettuce 기본값(60초) 대신 1초 만에 실패시켜 요청 스레드가 묶이지 않게 한다.
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisCommandTimeout() {
        return builder -> builder.commandTimeout(COMMAND_TIMEOUT);
    }
}
