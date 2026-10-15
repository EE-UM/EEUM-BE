package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.Notification;
import com.eeum.domain.notification.fcm.entity.NotificationType;
import com.eeum.domain.notification.fcm.entity.TargetType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByTypeAndTargetTypeAndTargetId(NotificationType type, TargetType targetType,
        Long targetId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = """
              UPDATE notification
              SET status = 'SENDING', locked_until = :leaseUntil
              WHERE id = :id
                AND (status = 'PENDING'
                OR (status = 'SENDING' AND locked_until < :now))
        """, nativeQuery = true)
    int claim(@Param("id") Long id,
        @Param("now") LocalDateTime now,
        @Param("leaseUntil") LocalDateTime leaseUntil);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = """
                UPDATE notification
                SET status = 'SENT', sent_at = :now, locked_until = NULL
                WHERE id = :id AND status = 'SENDING'
        """, nativeQuery = true)
    int markSent(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = """
                UPDATE notification
                SET status = 'FAILED', locked_until = NULL
                WHERE id = :id AND status = 'SENDING'
        """, nativeQuery = true)
    int markFailed(@Param("id") Long id);

    @Query(value = """
                SELECT id FROM notification
                WHERE (status = 'PENDING' AND type <> 'ANNOUNCEMENT' AND created_at < :pendingBefore)
                OR (status = 'SENDING' AND locked_until < :now)
                ORDER BY id
                LIMIT :limit
        """, nativeQuery = true)
    List<Long> findDispatchTargetIds(@Param("pendingBefore") LocalDateTime pendingBefore,
        @Param("now") LocalDateTime now,
        @Param("limit") int limit);

    @Query(value = """
        SELECT id FROM notification
        WHERE status = 'PENDING' AND type = 'ANNOUNCEMENT'
        ORDER BY id
        LIMIT :limit
        """, nativeQuery = true)
    List<Long> findPendingAnnouncementIds(@Param("limit") int limit);
}
