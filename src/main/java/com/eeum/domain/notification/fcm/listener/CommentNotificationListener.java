package com.eeum.domain.notification.fcm.listener;

import com.eeum.domain.comment.event.CommentLimitReachedEvent;
import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
import com.eeum.domain.comment.event.HalfCommentReachedEvent;
import com.eeum.domain.notification.fcm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentNotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handle(FirstCommentCreatedEvent event) {
        notificationService.createFirstComment(event);
    }

    @EventListener
    public void handle(HalfCommentReachedEvent event) {
        notificationService.createHalfComment(event);
    }

    @EventListener
    public void handle(CommentLimitReachedEvent event) {
        notificationService.createPostCompleted(event);
    }
}
