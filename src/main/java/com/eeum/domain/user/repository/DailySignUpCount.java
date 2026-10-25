package com.eeum.domain.user.repository;

public interface DailySignUpCount {

    /**
     * KST 기준 가입일 (yyyy-MM-dd)
     */
    String getSignUpDate();

    Long getSignUpCount();
}
