package com.eeum.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class NotificationExecutorConfig {

    private static final int SEND_CONCURRENCY = 10;

    @Bean
    public ThreadPoolTaskExecutor announcementSendExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(SEND_CONCURRENCY);
        executor.setMaxPoolSize(SEND_CONCURRENCY);
        executor.setThreadNamePrefix("announce-send-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
