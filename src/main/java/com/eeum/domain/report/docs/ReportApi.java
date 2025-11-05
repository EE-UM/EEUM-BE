package com.eeum.domain.report.docs;

import com.eeum.domain.report.dto.request.CommentReportRequest;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.dto.response.CommentReportResponse;
import com.eeum.domain.report.dto.response.PostsReportResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Report", description = "Report API")
public interface ReportApi {

    @Operation(summary = "게시글(플레이리스트) 신고", description = "게시글(플레이리스트)이 신고 처리 후 삭제됩니다.")
    ApiResponse<PostsReportResponse> reportPost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody PostsReportRequest postsReportRequest
    );

    @Operation(summary = "댓글 신고", description = "댓글(플레이리스트)이 신고 처리 후 삭제됩니다.")
    ApiResponse<CommentReportResponse> reportComment(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CommentReportRequest commentReportRequest
    );
}
