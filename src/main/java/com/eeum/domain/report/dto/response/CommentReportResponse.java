package com.eeum.domain.report.dto.response;

import com.eeum.domain.report.dto.request.CommentReportRequest;
import java.time.LocalDateTime;

public record CommentReportResponse(
        Long reporterUserId,
        Long reportedUserId,
        Long reportedCommentId,
        String reportReason,
        LocalDateTime reportTime
) {

    public static CommentReportResponse of(Long reporterUserId, CommentReportRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return new CommentReportResponse(reporterUserId, request.reportedUserId(), request.commentId(),
                request.reportReason(), now);
    }
}
