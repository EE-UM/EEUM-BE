package com.eeum.domain.report.dto.request;

public record CommentReportRequest(
        Long commentId,
        Long reportedUserId,
        String reportReason
) {
}
