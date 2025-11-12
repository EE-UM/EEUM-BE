package com.eeum.domain.common.webhook.discord;

import com.eeum.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageFormatter {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String SIGN_UP_MESSAGE =
            "```[%s] 신규 회원 가입이 발생했습니다.\n\n" +
                    "[가입자 정보]\n" +
                    "유저 Device ID : %s\n" +
                    "회원가입 유형 : %s\n" +
                    "개발 환경 : %s\n\n" +
                    "현재 시각 기준으로 시스템에 성공적으로 등록되었습니다.\n" +
                    "환영합니다! \uD83C\uDF89\n```";

    public static String formatSignUpMessage(String deviceId, String signupMethod, String environment) {
        return String.format(
                SIGN_UP_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                deviceId,
                signupMethod,
                environment
        );
    }
}
