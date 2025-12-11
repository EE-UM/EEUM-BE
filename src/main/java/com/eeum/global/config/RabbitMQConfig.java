package com.eeum.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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

    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_DLQ = "email.dlq";
    public static final String EMAIL_ROUTING_KEY = "email.routing";

    public static final String SPAM_FILTER_EXCHANGE = "spamFilter.exchange";
    public static final String SPAM_FILTER_QUEUE = "spamFilter.queue";
    public static final String SPAM_FILTER_DLQ = "spamFilter.dlq";
    public static final String SPAM_FILTER_ROUTING_KEY = "spamFilter.routing";

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

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

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDeadLetterQueue())
                .to(emailExchange())
                .with(EMAIL_DLQ);
    }

    @Bean
    public Queue spamFilterQueue() {
        return QueueBuilder.durable(SPAM_FILTER_QUEUE)
                .withArgument("x-dead-letter-exchange", SPAM_FILTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SPAM_FILTER_DLQ)
                .build();
    }

    @Bean
    public Queue spamFilterDeadLetterQueue() {
        return QueueBuilder.durable(SPAM_FILTER_DLQ).build();
    }

    @Bean
    public DirectExchange spamFilterExchange() {
        return new DirectExchange(SPAM_FILTER_EXCHANGE);
    }

    @Bean
    public Binding spamFilterBinding() {
        return BindingBuilder.bind(spamFilterQueue())
                .to(spamFilterExchange())
                .with(SPAM_FILTER_ROUTING_KEY);
    }

    @Bean
    public Binding spamFilterDlqBinding() {
        return BindingBuilder.bind(spamFilterDeadLetterQueue())
                .to(spamFilterExchange())
                .with(SPAM_FILTER_DLQ);
    }
}
