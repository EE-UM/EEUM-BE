package com.eeum.domain.notification.publisher;

import com.eeum.domain.common.spamfilter.service.SpamFilterService;
import com.eeum.domain.notification.dto.request.SpamFilterRequest;
import com.eeum.global.redis.RedisFeature;
import com.eeum.global.redis.RedisGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpamFilterPublisher {

    private static final String CHANNEL = "notification:spamfilter";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisGuard redisGuard;
    private final SpamFilterService spamFilterService;
    private final TaskExecutor taskExecutor;

    public void publish(SpamFilterRequest spamFilterRequest) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(spamFilterRequest);
            boolean published = redisGuard.call(RedisFeature.SPAM_PUBSUB, () -> {
                redisTemplate.convertAndSend(CHANNEL, jsonMessage);
                return true;
            }, () -> false);

            if (published) {
                log.info("스팸 필터 알림 발행 완료 - 채널: {}", CHANNEL);
            } else {
                filterDirectly(spamFilterRequest);
            }
        } catch (JacksonException e) {
            log.error("[Error] 스팸 알림 메시지 직렬화 실패. ", e);
        }
    }

    /**
     * Redis를 쓸 수 없을 때 Pub/Sub 대신 같은 서버의 비동기 스레드에서 스팸 필터를 실행한다.
     */
    private void filterDirectly(SpamFilterRequest request) {
        taskExecutor.execute(() -> {
            try {
                spamFilterService.spamPostFilter(request.postId(), request.content());
                log.info("[SpamFilterPublisher] Redis 대신 직접 스팸 필터 처리 완료 - postId={}",
                    request.postId());
            } catch (Exception e) {
                log.error("[SpamFilterPublisher] 직접 스팸 필터 처리 실패 - postId={}",
                    request.postId(), e);
            }
        });
    }
}
