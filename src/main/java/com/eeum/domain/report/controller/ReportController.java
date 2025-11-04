package com.eeum.domain.report.controller;

import com.eeum.domain.report.application.ReportFacade;
import com.eeum.domain.report.docs.ReportApi;
import com.eeum.domain.report.dto.request.CommentReportRequest;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.dto.response.CommentReportResponse;
import com.eeum.domain.report.dto.response.PostsReportResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.global.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController implements ReportApi {

    private final ReportFacade reportFacade;

    @PostMapping("/posts")
    public ApiResponse<PostsReportResponse> reportPost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody PostsReportRequest postsReportRequest
    ) {
        return ApiResponse.success(reportFacade.reportPosts(userPrincipal.getId(), postsReportRequest));
    }

    @PostMapping("/comment")
    public ApiResponse<CommentReportResponse> reportComment(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CommentReportRequest commentReportRequest
            ) {
        return ApiResponse.success(reportFacade.reportComment(userPrincipal.getId(), commentReportRequest));
    }
}
