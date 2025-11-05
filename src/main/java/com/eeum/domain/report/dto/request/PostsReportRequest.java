package com.eeum.domain.report.dto.request;

public record PostsReportRequest(
        Long postId,
        Long reportedUserId,
        String reportReason
) {
}
