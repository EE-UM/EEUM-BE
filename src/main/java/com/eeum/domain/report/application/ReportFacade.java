package com.eeum.domain.report.application;

import static com.eeum.domain.report.constant.ReportType.*;

import com.eeum.domain.common.constant.DiscordWebhookType;
import com.eeum.domain.common.webhook.discord.DiscordWebhookResponse;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.common.webhook.discord.message.ReportMessageFormatter;
import com.eeum.domain.notification.dto.request.MailRequest;
import com.eeum.domain.notification.producer.MailProducer;
import com.eeum.domain.notification.service.MailService;
import com.eeum.domain.report.dto.request.CommentReportRequest;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.dto.response.CommentReportResponse;
import com.eeum.domain.report.dto.response.PostsReportResponse;
import com.eeum.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportFacade {

    private final ReportService reportService;
    private final MessageService messageService;
    private final MailService mailService;
    private final MailProducer mailProducer;

    public PostsReportResponse reportPosts(Long reporterUserId, PostsReportRequest postsReportRequest) {
        reportService.postsReport(reporterUserId, postsReportRequest);

        sendDiscordNotification(String.valueOf(reporterUserId), String.valueOf(postsReportRequest.reportedUserId()),
                POSTS.getStatus(), String.valueOf(postsReportRequest.postId()),
                postsReportRequest.reportReason());

        MailRequest mailRequest = MailRequest.of(
                "hazardous10@naver.com",
                "신고 발생: " + postsReportRequest.postId(),
                "<p>신고자 ID: " + reporterUserId + "</p><p>신고 이유: " + postsReportRequest.reportReason() + "</p>"
        );
        mailProducer.publishMail(mailRequest);

        return PostsReportResponse.of(reporterUserId, postsReportRequest);
    }

    public CommentReportResponse reportComment(Long reporterUserId, CommentReportRequest commentReportRequest) {
        reportService.commentReport(reporterUserId, commentReportRequest);

        sendDiscordNotification(String.valueOf(reporterUserId), String.valueOf(commentReportRequest.reportedUserId()),
                COMMENT.getStatus(), String.valueOf(commentReportRequest.commentId()),
                String.valueOf(commentReportRequest.reportReason()));

        return CommentReportResponse.of(reporterUserId, commentReportRequest);
    }

    private void sendDiscordNotification(String reporterUserId, String reportedUserId, String reportType,
                                         String reportedPostsOrCommentId, String reportReason) {
        messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(ReportMessageFormatter.formatSignUpMessage(
                reporterUserId,
                reportedUserId,
                reportType,
                reportedPostsOrCommentId,
                reportReason
        )), DiscordWebhookType.REPORT);
    }
}
