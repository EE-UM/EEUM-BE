package com.eeum.domain.comment.producer;

import com.eeum.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendCompletedPost(Long postId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PLAYLIST_EXCHANGE,
                RabbitMQConfig.RK_PLAYLIST_COMPLETED,
                postId
        );
    }
}
