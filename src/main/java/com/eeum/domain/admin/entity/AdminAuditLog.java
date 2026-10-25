package com.eeum.domain.admin.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 관리자 조치 이력 (누가, 언제, 어떤 대상에, 왜). 수정·삭제하지 않는 append-only 테이블.
 */
@Table(name = "admin_audit_log")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {

    @Id
    @Tsid
    private Long id;

    private Long adminUserId;

    @Enumerated(EnumType.STRING)
    private AdminAction action;

    @Enumerated(EnumType.STRING)
    private AuditTargetType targetType;

    private Long targetId;

    private String reason;

    private String ip;

    private LocalDateTime createdAt;

    public static AdminAuditLog of(Long adminUserId, AdminAction action,
        AuditTargetType targetType, Long targetId, String reason, String ip) {
        return AdminAuditLog.builder()
            .adminUserId(adminUserId)
            .action(action)
            .targetType(targetType)
            .targetId(targetId)
            .reason(reason)
            .ip(ip)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private AdminAuditLog(Long adminUserId, AdminAction action, AuditTargetType targetType,
        Long targetId, String reason, String ip, LocalDateTime createdAt) {
        this.adminUserId = adminUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.ip = ip;
        this.createdAt = createdAt;
    }
}
