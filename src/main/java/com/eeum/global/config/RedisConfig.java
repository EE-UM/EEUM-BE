package com.eeum.global.config;

import com.eeum.domain.notification.subscriber.MailSubscriber;
import com.eeum.domain.notification.subscriber.PostsCompletedSubscriber;
import com.eeum.domain.notification.subscriber.SpamFilterSubscriber;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    private static final Duration KEEP_ALIVE_IDLE = Duration.ofSeconds(5);
    private static final Duration KEEP_ALIVE_INTERVAL = Duration.ofSeconds(5);
    private static final int KEEP_ALIVE_COUNT = 3;

    @Bean
    public LettuceClientOptionsBuilderCustomizer redisKeepAlive() {
        SocketOptions.KeepAliveOptions keepAliveOptions = SocketOptions.KeepAliveOptions.builder()
            .enable()
            .idle(KEEP_ALIVE_IDLE)
            .interval(KEEP_ALIVE_INTERVAL)
            .count(KEEP_ALIVE_COUNT)
            .build();

        return builder -> builder.socketOptions(SocketOptions.builder()
            .keepAlive(keepAliveOptions)
            .build());
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        MailSubscriber mailSubscriber,
        SpamFilterSubscriber spamFilterSubscriber,
        PostsCompletedSubscriber postsCompletedSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(mailSubscriber, new ChannelTopic("notification:mail"));
        container.addMessageListener(spamFilterSubscriber,
            new ChannelTopic("notification:spamfilter"));
        container.addMessageListener(postsCompletedSubscriber,
            new ChannelTopic("notification:playlist"));

        return container;
    }
}
