package com.eeum.domain.common.webhook.discord.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportMessageFormatter {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String SIGN_UP_MESSAGE =
            "```[%s] 게시글/댓글 신고가 발생했습니다.\n\n" +
                    "[신고 정보]\n" +
                    "신고한 유저 ID : %s\n" +
                    "신고당한 유저 ID : %s\n" +
                    "신고 유형(게시글/댓글) : %s\n" +
                    "신고 게시물/댓글 ID : %s\n" +
                    "신고 이유 : %s\n" +
                    "신고 받은 게시글/코멘트 내용: %s\n\n" +
                    "신고 받은 게시물/댓글은 소프트 딜리트 처리되었습니다.\n```";

    public static String formatSignUpMessage(String reporterUserId, String reportedUserId, String reportType, String reportedPostsOrCommentId, String reportReason, String content) {
        return String.format(
                SIGN_UP_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                reporterUserId,
                reportedUserId,
                reportType,
                reportedPostsOrCommentId,
                reportReason,
                content
        );
    }
}
