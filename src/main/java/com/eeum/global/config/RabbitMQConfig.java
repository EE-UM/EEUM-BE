package com.eeum.global.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PLAYLIST_COMPLETED_QUEUE = "playlist_completed_queue";
    public static final String PLAYLIST_EXCHANGE = "playlist_exchange";
    public static final String DLQ = "deadLetterQueue";
    public static final String DLX = "deadLetterExchange";

    public static final String RK_PLAYLIST_COMPLETED = "playlist.completed";
    public static final String RK_DLQ = "playlist.dlq";

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    @Bean
    public Queue playlistQueue() {
        return QueueBuilder.durable(PLAYLIST_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RK_DLQ)
                .ttl(5000)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ);
    }

    @Bean
    public Binding playlistCompletedBinding() {
        return BindingBuilder.bind(playlistQueue()).to(playlistExchange()).with(RK_PLAYLIST_COMPLETED);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(RK_DLQ);
    }
}
