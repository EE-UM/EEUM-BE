package com.eeum.domain.admin.dto.response;

import com.eeum.domain.admin.entity.AdminAuditLog;
import com.eeum.domain.admin.repository.AdminPostRow;
import com.eeum.domain.report.entity.PostsReport;
import java.util.List;

public record AdminPostDetailResponse(
    AdminPostRow post,
    List<PostsReport> reports,
    List<AdminAuditLog> auditLogs
) {
}
