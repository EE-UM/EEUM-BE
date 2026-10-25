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
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(1);

    @Bean
    public RedisFailureDetector redisFailureDetector() {
        return new RedisFailureDetector(FAILURE_THRESHOLD, BYPASS_DURATION, System::nanoTime);
    }

    @Bean
    public LettuceClientOptionsBuilderCustomizer rejectCommandsWhenDisconnected() {
        return builder -> builder.disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS);
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisCommandTimeout() {
        return builder -> builder.commandTimeout(COMMAND_TIMEOUT);
    }
}
