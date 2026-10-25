package com.eeum.domain.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AdminDashboardResponse(
    long totalUsers,
    long totalPosts,
    long completedPosts,
    long reportDeletedPosts,
    List<DailySignUp> dailySignUps
) {

    public record DailySignUp(LocalDate date, long count) {
    }

    public long signUpsInPeriod() {
        return dailySignUps.stream().mapToLong(DailySignUp::count).sum();
    }

    /**
     * 차트 y축 최댓값. 일별 최대 가입 수 이상인 1·2·5 × 10^n 중 가장 작은 값 (최소 1).
     */
    public long chartMax() {
        long max = dailySignUps.stream().mapToLong(DailySignUp::count).max().orElse(0L);
        long magnitude = 1;
        while (true) {
            for (long step : new long[]{1, 2, 5}) {
                if (step * magnitude >= max) {
                    return step * magnitude;
                }
            }
            magnitude *= 10;
        }
    }

    public int barHeightPercent(DailySignUp dailySignUp) {
        return (int) Math.round(dailySignUp.count() * 100.0 / chartMax());
    }
}
