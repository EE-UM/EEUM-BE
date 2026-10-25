package com.eeum.domain.notification.fcm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.comment.event.CommentLimitReachedEvent;
import com.eeum.domain.comment.event.HalfCommentReachedEvent;
import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationSetting;
import com.eeum.domain.notification.fcm.entity.NotificationStatus;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.entity.TargetType;
import com.eeum.domain.notification.fcm.event.NotificationCreatedEvent;
import com.eeum.domain.notification.fcm.repository.NotificationRepository;
import com.eeum.domain.notification.fcm.repository.NotificationSettingRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Long POST_ID = 1L;
    private static final Long AUTHOR_ID = 10L;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("절반 도달 시 작성자에게 HALF_COMMENT 알림을 PENDING 으로 저장하고 전송 이벤트를 발행한다")
    void createHalfComment_savesPendingNotification() {
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        notificationService.createHalfComment(new HalfCommentReachedEvent(POST_ID, AUTHOR_ID, "제목"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(AUTHOR_ID);
        assertThat(saved.getType()).isEqualTo(NotificationType.HALF_COMMENT);
        assertThat(saved.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(saved.getTargetId()).isEqualTo(POST_ID);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getBody()).contains("'제목'");
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("목표 달성 알림은 POST_COMPLETED 유형으로 저장한다")
    void createPostCompleted_savesPostCompletedType() {
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        notificationService.createPostCompleted(new CommentLimitReachedEvent(POST_ID, AUTHOR_ID, "제목"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.POST_COMPLETED);
    }

    @Test
    @DisplayName("같은 게시글에 같은 유형 알림이 이미 있으면 다시 저장하지 않는다 (댓글 삭제 후 재도달)")
    void createHalfComment_skipsDuplicate() {
        given(notificationRepository.existsByTypeAndTargetTypeAndTargetId(
            NotificationType.HALF_COMMENT, TargetType.POST, POST_ID)).willReturn(true);

        notificationService.createHalfComment(new HalfCommentReachedEvent(POST_ID, AUTHOR_ID, "제목"));

        verify(notificationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("작성자가 해당 유형 알림을 꺼 두었으면 저장하지 않는다")
    void createPostCompleted_skipsWhenDisabled() {
        NotificationSetting setting = NotificationSetting.createDefault(AUTHOR_ID,
            NotificationType.POST_COMPLETED);
        setting.changeEnabled(false);
        given(notificationSettingRepository.findByUserIdAndType(AUTHOR_ID,
            NotificationType.POST_COMPLETED)).willReturn(Optional.of(setting));

        notificationService.createPostCompleted(new CommentLimitReachedEvent(POST_ID, AUTHOR_ID, "제목"));

        verify(notificationRepository, never()).save(any());
    }
}
