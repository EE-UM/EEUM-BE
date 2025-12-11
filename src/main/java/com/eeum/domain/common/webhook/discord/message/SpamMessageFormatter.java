package com.eeum.domain.common.webhook.discord.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SpamMessageFormatter {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String SPAM_MESSAGE_LEVEL1 =
            "```[%s] 스팸(홍보/욕설 등) 게시글이 작성되었습니다.\n\n" +
                    "[스팸 정보]\n" +
                    "작성한 유저 ID : %s\n" +
                    "스팸 게시글 ID : %s\n" +
                    "스팸 의심도(1: 의심, 2: 확실) : %s\n" +
                    "스팸 게시글 내용: %s\n\n" +
                    "해당 게시글은 스팸 의심이 가는 게시글입니다. 관리자의 직접 확인이 필요합니다.\n```";

    private static final String SPAM_MESSAGE_LEVEL2 =
            "```[%s] 스팸(홍보/욕설 등) 게시글이 작성되었습니다.\n\n" +
                    "[스팸 정보]\n" +
                    "작성한 유저 ID : %s\n" +
                    "스팸 게시글 ID : %s\n" +
                    "스팸 의심도(1: 의심, 2: 확실) : %s\n" +
                    "스팸 게시글 내용: %s\n\n" +
                    "해당 게시글은 확실히 스팸으로 분리되어 소프트 딜리트 처리되었습니다.\n```";

    public static String formatSpamMessageLevel1(String userId, String postId, String doubtLevel, String content) {
        return String.format(
                SPAM_MESSAGE_LEVEL1,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                userId,
                postId,
                doubtLevel,
                content
        );
    }

    public static String formatSpamMessageLevel2(String userId, String postId, String doubtLevel, String content) {
        return String.format(
                SPAM_MESSAGE_LEVEL2,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                userId,
                postId,
                doubtLevel,
                content
        );
    }
}
