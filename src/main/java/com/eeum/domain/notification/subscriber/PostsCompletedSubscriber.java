package com.eeum.domain.notification.subscriber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsCompletedSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String postId = new String(message.getBody());
            log.info("[PostsCompletedSubscriber] 게시물 완료 알림 수신 - postId={}", postId);
        } catch (Exception e) {
            log.error("[PostsCompletedSubscriber] 메시지 처리 실패", e);
        }
    }
}
