package com.eeum.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.admin.dto.request.AdminPostSearchCondition;
import com.eeum.domain.admin.dto.request.AdminPostStatus;
import com.eeum.domain.admin.entity.AdminAction;
import com.eeum.domain.admin.entity.AdminAuditLog;
import com.eeum.domain.admin.entity.AuditTargetType;
import com.eeum.domain.admin.exception.AdminPostNotFoundException;
import com.eeum.domain.admin.exception.InvalidAdminActionException;
import com.eeum.domain.admin.repository.AdminAuditLogRepository;
import com.eeum.domain.admin.repository.AdminPostQueryRepository;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.service.PostsDeletionService;
import com.eeum.domain.report.repository.PostsReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminPostServiceTest {

    private static final Long POST_ID = 100L;
    private static final Long ADMIN_ID = 7L;
    private static final String IP = "203.0.113.10";

    @InjectMocks
    private AdminPostService adminPostService;

    @Mock
    private AdminPostQueryRepository adminPostQueryRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private PostsReportRepository postsReportRepository;

    @Mock
    private PostsDeletionService postsDeletionService;

    private AdminAuditLog savedAuditLog() {
        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("관리자 삭제는 ADMIN 사유로 soft delete 하고, 사유를 다듬어 감사 로그에 남긴다")
    void delete_softDeletesAndWritesAuditLog() {
        given(postsDeletionService.softDelete(POST_ID, ADMIN_ID, DeleteReason.ADMIN))
            .willReturn(true);

        adminPostService.delete(POST_ID, ADMIN_ID, "  광고성 게시글  ", IP);

        AdminAuditLog log = savedAuditLog();
        assertThat(log.getAdminUserId()).isEqualTo(ADMIN_ID);
        assertThat(log.getAction()).isEqualTo(AdminAction.POST_DELETE);
        assertThat(log.getTargetType()).isEqualTo(AuditTargetType.POST);
        assertThat(log.getTargetId()).isEqualTo(POST_ID);
        assertThat(log.getReason()).isEqualTo("광고성 게시글");
        assertThat(log.getIp()).isEqualTo(IP);
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 삭제하면 예외가 발생하고 감사 로그를 남기지 않는다")
    void delete_alreadyDeleted_throwsWithoutAuditLog() {
        given(postsDeletionService.softDelete(POST_ID, ADMIN_ID, DeleteReason.ADMIN))
            .willReturn(false);

        assertThatThrownBy(() -> adminPostService.delete(POST_ID, ADMIN_ID, "사유", IP))
            .isInstanceOf(InvalidAdminActionException.class);

        verify(adminAuditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("사유가 비어 있거나 500자를 넘으면 삭제·복구하지 않는다")
    void invalidReason_isRejectedBeforeAnyChange() {
        assertThatThrownBy(() -> adminPostService.delete(POST_ID, ADMIN_ID, "   ", IP))
            .isInstanceOf(InvalidAdminActionException.class);
        assertThatThrownBy(() -> adminPostService.delete(POST_ID, ADMIN_ID, null, IP))
            .isInstanceOf(InvalidAdminActionException.class);
        assertThatThrownBy(() -> adminPostService.restore(POST_ID, ADMIN_ID, "a".repeat(501), IP))
            .isInstanceOf(InvalidAdminActionException.class);

        verify(postsDeletionService, never()).softDelete(any(), any(), any());
        verify(postsDeletionService, never()).restore(any());
        verify(adminAuditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("복구에 성공하면 POST_RESTORE 감사 로그를 남긴다")
    void restore_writesAuditLog() {
        given(postsDeletionService.restore(POST_ID)).willReturn(true);

        adminPostService.restore(POST_ID, ADMIN_ID, "오신고 확인", IP);

        AdminAuditLog log = savedAuditLog();
        assertThat(log.getAction()).isEqualTo(AdminAction.POST_RESTORE);
        assertThat(log.getReason()).isEqualTo("오신고 확인");
    }

    @Test
    @DisplayName("삭제 상태가 아닌 게시글을 복구하면 예외가 발생하고 감사 로그를 남기지 않는다")
    void restore_notDeleted_throwsWithoutAuditLog() {
        given(postsDeletionService.restore(POST_ID)).willReturn(false);

        assertThatThrownBy(() -> adminPostService.restore(POST_ID, ADMIN_ID, "사유", IP))
            .isInstanceOf(InvalidAdminActionException.class);

        verify(adminAuditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("검색은 상태 이름과 LIKE 와일드카드를 이스케이프한 키워드로 20개씩 조회한다")
    void search_escapesKeywordAndPages() {
        adminPostService.search(
            new AdminPostSearchCondition(AdminPostStatus.DELETED, " 100%_off ", 5L, 2));

        verify(adminPostQueryRepository).search("DELETED", 5L, "100\\%\\_off",
            PageRequest.of(2, AdminPostService.PAGE_SIZE));
    }

    @Test
    @DisplayName("검색 조건이 비어 있으면 전체 상태, 키워드 없음, 첫 페이지로 조회한다")
    void search_defaults() {
        AdminPostSearchCondition condition = new AdminPostSearchCondition(null, "  ", null, -1);

        assertThat(condition.status()).isEqualTo(AdminPostStatus.ALL);
        assertThat(condition.keyword()).isNull();
        assertThat(condition.escapedKeyword()).isNull();
        assertThat(condition.page()).isZero();
    }

    @Test
    @DisplayName("없는 게시글 상세를 조회하면 AdminPostNotFoundException 이 발생한다")
    void getDetail_notFound() {
        given(adminPostQueryRepository.findRowById(POST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPostService.getDetail(POST_ID))
            .isInstanceOf(AdminPostNotFoundException.class);
    }
}
