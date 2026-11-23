package com.eeum.domain.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 웹 가입 폼 (앱 TermsAgreementScreen + ProfileRegistrationScreen). 체크박스는 체크하지 않으면 값이 오지 않으므로 null 도 거부한다.
 */
public record WebSignupForm(
    Boolean agreeTerms,
    Boolean agreePrivacy,
    Boolean agreeCommunity,
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = NICKNAME_MAX_LENGTH, message = "닉네임은 30자까지 쓸 수 있어요.")
    String nickname
) {

    public static final int NICKNAME_MAX_LENGTH = 30;

    public static WebSignupForm empty() {
        return new WebSignupForm(false, false, false, "");
    }

    @AssertTrue(message = "필수 약관에 모두 동의해 주세요.")
    public boolean isAllAgreed() {
        return Boolean.TRUE.equals(agreeTerms) && Boolean.TRUE.equals(agreePrivacy)
            && Boolean.TRUE.equals(agreeCommunity);
    }
}
