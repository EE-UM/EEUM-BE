package com.eeum.domain.admin.repository;

import com.eeum.domain.admin.entity.AdminAuditLog;
import com.eeum.domain.admin.entity.AuditTargetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findByTargetTypeAndTargetIdOrderByIdDesc(AuditTargetType targetType,
        Long targetId);
}
