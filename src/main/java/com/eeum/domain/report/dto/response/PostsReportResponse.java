package com.eeum.domain.report.dto.response;

import com.eeum.domain.report.dto.request.PostsReportRequest;
import java.time.LocalDateTime;

public record PostsReportResponse(
        Long reporterUserId,
        Long reportedUserId,
        Long reportedPostId,
        String reportReason,
        LocalDateTime reportTime
) {

    public static PostsReportResponse of(Long reporterUserId, PostsReportRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return new PostsReportResponse(reporterUserId, request.reportedUserId(), request.postId(),
                request.reportReason(), now);
    }
}
