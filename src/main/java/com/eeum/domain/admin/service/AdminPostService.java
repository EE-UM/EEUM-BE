package com.eeum.domain.admin.service;

import com.eeum.domain.admin.dto.request.AdminPostSearchCondition;
import com.eeum.domain.admin.dto.response.AdminPostDetailResponse;
import com.eeum.domain.admin.entity.AdminAction;
import com.eeum.domain.admin.entity.AdminAuditLog;
import com.eeum.domain.admin.entity.AuditTargetType;
import com.eeum.domain.admin.exception.AdminPostNotFoundException;
import com.eeum.domain.admin.exception.InvalidAdminActionException;
import com.eeum.domain.admin.repository.AdminAuditLogRepository;
import com.eeum.domain.admin.repository.AdminPostQueryRepository;
import com.eeum.domain.admin.repository.AdminPostRow;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.service.PostsDeletionService;
import com.eeum.domain.report.repository.PostsReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostService {

    public static final int PAGE_SIZE = 20;
    public static final int MAX_REASON_LENGTH = 500;

    private final AdminPostQueryRepository adminPostQueryRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final PostsReportRepository postsReportRepository;
    private final PostsDeletionService postsDeletionService;

    public Page<AdminPostRow> search(AdminPostSearchCondition condition) {
        return adminPostQueryRepository.search(condition.status().name(), condition.userId(),
            condition.escapedKeyword(), PageRequest.of(condition.page(), PAGE_SIZE));
    }

    public AdminPostDetailResponse getDetail(Long postId) {
        AdminPostRow post = adminPostQueryRepository.findRowById(postId)
            .orElseThrow(() -> new AdminPostNotFoundException(postId));
        return new AdminPostDetailResponse(
            post,
            postsReportRepository.findByPostIdOrderByCreatedAtDesc(postId),
            adminAuditLogRepository.findByTargetTypeAndTargetIdOrderByIdDesc(
                AuditTargetType.POST, postId)
        );
    }

    /**
     * 게시글을 ADMIN 사유로 soft delete 하고 같은 트랜잭션에서 감사 로그를 남긴다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long postId, Long adminUserId, String reason, String ip) {
        String validReason = validateReason(reason);
        if (!postsDeletionService.softDelete(postId, adminUserId, DeleteReason.ADMIN)) {
            throw new InvalidAdminActionException("이미 삭제되었거나 존재하지 않는 게시글입니다.");
        }

        adminAuditLogRepository.save(AdminAuditLog.of(adminUserId, AdminAction.POST_DELETE,
            AuditTargetType.POST, postId, validReason, ip));
        log.info("[Admin] 게시글 삭제 - postId={}, adminUserId={}", postId, adminUserId);
    }

    /**
     * 삭제된 게시글을 복구하고 같은 트랜잭션에서 감사 로그를 남긴다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void restore(Long postId, Long adminUserId, String reason, String ip) {
        String validReason = validateReason(reason);
        if (!postsDeletionService.restore(postId)) {
            throw new InvalidAdminActionException("삭제 상태가 아니거나 존재하지 않는 게시글입니다.");
        }

        adminAuditLogRepository.save(AdminAuditLog.of(adminUserId, AdminAction.POST_RESTORE,
            AuditTargetType.POST, postId, validReason, ip));
        log.info("[Admin] 게시글 복구 - postId={}, adminUserId={}", postId, adminUserId);
    }

    private String validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new InvalidAdminActionException("사유를 입력해 주세요.");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_REASON_LENGTH) {
            throw new InvalidAdminActionException(
                "사유는 " + MAX_REASON_LENGTH + "자 이하로 입력해 주세요.");
        }
        return trimmed;
    }
}
