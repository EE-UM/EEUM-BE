package com.eeum.domain.admin.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 관리자 집계용 KST 기간 계산. DB 의 created_at 등은 JVM 기본 시간대(운영 컨테이너 UTC, 로컬 KST)로 저장되므로,
 * KST 날짜 경계를 저장 시간대의 LocalDateTime 으로 바꾸고, 저장 시각을 KST 날짜로 묶기 위한 보정 초(offset)를 제공한다.
 *
 * @param clock 저장 시간대(JVM 기본 시간대)의 시계
 */
public record KstPeriod(Clock clock) {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static KstPeriod systemDefault() {
        return new KstPeriod(Clock.systemDefaultZone());
    }

    public LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), KST);
    }

    /**
     * KST 날짜의 0시를 저장 시간대 기준 LocalDateTime 으로 변환한다. (created_at >= 이 값) 조건에 쓴다.
     */
    public LocalDateTime storedStartOf(LocalDate kstDate) {
        return kstDate.atStartOfDay(KST).withZoneSameInstant(clock.getZone()).toLocalDateTime();
    }

    /**
     * 지금으로부터 hours 시간 전을 저장 시간대 기준 LocalDateTime 으로 반환한다.
     */
    public LocalDateTime storedHoursAgo(long hours) {
        return LocalDateTime.ofInstant(clock.instant().minusSeconds(hours * 3600), clock.getZone());
    }

    /**
     * 저장 시각에 더하면 KST 시각이 되는 초. DATE_ADD(created_at, INTERVAL :offset SECOND) 로 KST 날짜를 구할 때 쓴다.
     */
    public int offsetSecondsToKst() {
        Instant now = clock.instant();
        return KST.getRules().getOffset(now).getTotalSeconds()
            - clock.getZone().getRules().getOffset(now).getTotalSeconds();
    }
}
