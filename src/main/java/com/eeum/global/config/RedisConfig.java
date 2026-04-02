package com.eeum.global.config;

import com.eeum.domain.notification.subscriber.MailSubscriber;
import com.eeum.domain.notification.subscriber.PostsCompletedSubscriber;
import com.eeum.domain.notification.subscriber.SpamFilterSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MailSubscriber mailSubscriber,
            SpamFilterSubscriber spamFilterSubscriber,
            PostsCompletedSubscriber postsCompletedSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(mailSubscriber, new ChannelTopic("notification:mail"));
        container.addMessageListener(spamFilterSubscriber, new ChannelTopic("notification:spamfilter"));
        container.addMessageListener(postsCompletedSubscriber, new ChannelTopic("notification:playlist"));

        return container;
    }
}
