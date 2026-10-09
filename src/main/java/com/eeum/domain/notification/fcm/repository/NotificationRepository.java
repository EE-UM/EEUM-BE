package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
