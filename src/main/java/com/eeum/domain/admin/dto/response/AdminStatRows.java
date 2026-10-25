package com.eeum.domain.admin.dto.response;

import java.time.LocalDateTime;

/**
 * 관리자 대시보드 집계 쿼리 결과 행들.
 */
public final class AdminStatRows {

    private AdminStatRows() {
    }

    public record RankedPost(Long postId, String title, String nickname, long value,
                             boolean completed) {
    }

    public record RecentReport(String type, Long targetId, Long postId, String content,
                               String reason, Long reporterUserId, LocalDateTime createdAt) {

        public boolean isPostReport() {
            return "POST".equals(type);
        }
    }

    public record RedisSwitchRow(String feature, boolean enabled, LocalDateTime updatedAt) {
    }

    public record BatchJobRow(String jobName, String status, int processedCount, int failedCount,
                              String errorMessage, LocalDateTime startedAt,
                              LocalDateTime finishedAt) {

        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }

    public record RecentAdminAction(LocalDateTime createdAt, Long adminUserId,
                                    String adminNickname, String action, Long targetId,
                                    String reason) {

        public String actionLabel() {
            return switch (action) {
                case "POST_DELETE" -> "게시글 삭제";
                case "POST_RESTORE" -> "게시글 복구";
                default -> action;
            };
        }
    }
}
