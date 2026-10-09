package com.eeum.domain.notification.fcm.repository;

import com.eeum.domain.notification.fcm.entity.DeadLetterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessage, Long> {

}
