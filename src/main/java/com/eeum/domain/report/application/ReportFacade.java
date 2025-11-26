package com.eeum.domain.report.application;

import static com.eeum.domain.report.constant.ReportType.*;

import com.eeum.domain.common.constant.DiscordWebhookType;
import com.eeum.domain.common.webhook.discord.DiscordWebhookResponse;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.common.webhook.discord.message.ReportMessageFormatter;
import com.eeum.domain.notification.dto.request.MailRequest;
import com.eeum.domain.notification.producer.MailProducer;
import com.eeum.domain.report.dto.request.CommentReportRequest;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.dto.response.CommentReportResponse;
import com.eeum.domain.report.dto.response.PostsReportResponse;
import com.eeum.domain.report.service.ReportService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportFacade {

    private final ReportService reportService;
    private final MessageService messageService;
    private final MailProducer mailProducer;

    public PostsReportResponse reportPosts(Long reporterUserId, PostsReportRequest postsReportRequest) {
        String postContent = reportService.postsReport(reporterUserId, postsReportRequest);

        sendDiscordNotification(String.valueOf(reporterUserId), String.valueOf(postsReportRequest.reportedUserId()),
                POSTS.getStatus(), String.valueOf(postsReportRequest.postId()),
                postsReportRequest.reportReason(),
                postContent);

        LocalDateTime now = LocalDateTime.now();
        MailRequest mailRequest = MailRequest.of(
                "fkffkffklallala@gmail.com",
                "게시글 신고 발생: 게시글 ID - " + postsReportRequest.postId(),
                "<p>신고자 ID: " + reporterUserId
                        + "</p><p>피신고자 ID: " + postsReportRequest.reportedUserId()
                        + "</p><p>신고 일시: " + now
                        + "</p>플레이리스트 본문: " + postContent
                        + "</p><p>신고 사유: " + postsReportRequest.reportReason() + "</p>"
        );
        mailProducer.publishMail(mailRequest);

        return PostsReportResponse.of(reporterUserId, postsReportRequest);
    }

    public CommentReportResponse reportComment(Long reporterUserId, CommentReportRequest commentReportRequest) {
        String commentContent = reportService.commentReport(reporterUserId, commentReportRequest);

        sendDiscordNotification(String.valueOf(reporterUserId), String.valueOf(commentReportRequest.reportedUserId()),
                COMMENT.getStatus(), String.valueOf(commentReportRequest.commentId()),
                String.valueOf(commentReportRequest.reportReason()),
                commentContent);

        LocalDateTime now = LocalDateTime.now();
        MailRequest mailRequest = MailRequest.of(
                "fkffkffklallala@gmail.com",
                "코멘트 신고 발생: 코멘트 ID - " + commentReportRequest.commentId(),
                "<p>신고자 ID: " + reporterUserId
                        + "</p><p>피신고자 ID: " + commentReportRequest.reportedUserId()
                        + "</p><p>신고 일시: " + now
                        + "</p>코멘트 본문: " + commentContent
                        + "</p><p>신고 사유: " + commentReportRequest.reportReason() + "</p>"
        );
        mailProducer.publishMail(mailRequest);

        return CommentReportResponse.of(reporterUserId, commentReportRequest);
    }

    private void sendDiscordNotification(String reporterUserId, String reportedUserId, String reportType,
                                         String reportedPostsOrCommentId, String reportReason, String content) {
        messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(ReportMessageFormatter.formatSignUpMessage(
                reporterUserId,
                reportedUserId,
                reportType,
                reportedPostsOrCommentId,
                reportReason,
                content
        )), DiscordWebhookType.REPORT);
    }
}
