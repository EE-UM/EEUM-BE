package com.eeum.domain.admin.dto.response;

import com.eeum.domain.admin.dto.response.AdminStatRows.BatchJobRow;
import com.eeum.domain.admin.dto.response.AdminStatRows.RankedPost;
import com.eeum.domain.admin.dto.response.AdminStatRows.RecentAdminAction;
import com.eeum.domain.admin.dto.response.AdminStatRows.RecentReport;
import com.eeum.domain.admin.dto.response.AdminStatRows.RedisSwitchRow;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public record AdminDashboardResponse(
    LocalDate today,
    List<TodayMetric> todayMetrics,
    Totals totals,
    DailySeries signUps,
    DailySeries posts,
    DailySeries comments,
    List<Share> usersByProvider,
    List<Share> usersByStatus,
    List<Share> postsByState,
    List<Share> completionTypes,
    List<RankedPost> topByComments,
    List<RankedPost> topByLikes,
    List<RankedPost> topByViews,
    Reports reports,
    Operations operations,
    List<RecentAdminAction> recentAdminActions
) {

    private static final DateTimeFormatter TODAY_FORMAT =
        DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN);

    public String todayLabel() {
        return today.format(TODAY_FORMAT);
    }

    /**
     * 오늘(KST) 수치와 어제 대비 증감.
     */
    public record TodayMetric(String label, long today, long yesterday) {

        public long delta() {
            return today - yesterday;
        }

        public String deltaText() {
            long delta = delta();
            if (delta == 0) {
                return "어제와 같아요";
            }
            return "어제보다 " + Math.abs(delta) + (delta > 0 ? " ▲" : " ▼");
        }
    }

    public record Totals(long users, long activePosts, long inProgressPosts, long completedPosts,
                         long deletedPosts, long comments, long likes, long views, long blocks) {
    }

    public record DailyCount(LocalDate date, long count) {
    }

    /**
     * 최근 N일 일별 수. 막대 차트 높이 계산을 함께 제공한다.
     */
    public record DailySeries(List<DailyCount> days) {

        public long total() {
            return days.stream().mapToLong(DailyCount::count).sum();
        }

        /**
         * 차트 y축 최댓값. 일별 최댓값 이상인 1·2·5 × 10^n 중 가장 작은 값 (최소 1).
         */
        public long chartMax() {
            long max = days.stream().mapToLong(DailyCount::count).max().orElse(0L);
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

        public int barHeightPercent(DailyCount day) {
            return (int) Math.round(day.count() * 100.0 / chartMax());
        }
    }

    /**
     * 구성 비율 한 줄 (라벨, 수, 전체 대비 %).
     */
    public record Share(String label, long count, int percent) {

        /**
         * 맵의 순서를 유지하며 비율을 계산한다. 라벨은 labeler 로 변환한다.
         */
        public static List<Share> of(Map<String, Long> counts, Function<String, String> labeler) {
            long total = counts.values().stream().mapToLong(Long::longValue).sum();
            return counts.entrySet().stream()
                .map(e -> new Share(labeler.apply(e.getKey()), e.getValue(),
                    total == 0 ? 0 : (int) Math.round(e.getValue() * 100.0 / total)))
                .toList();
        }
    }

    public record Reports(long totalPostReports, long totalCommentReports,
                          List<RecentReport> recent) {
    }

    public record Operations(long deviceTokens, List<Share> notificationsLast7Days,
                             long failedNotificationsLast7Days, List<RedisSwitchRow> redisSwitches,
                             List<BatchJobRow> batchJobs, long failedBatchJobsLast24Hours) {

        public boolean allRedisSwitchesOn() {
            return redisSwitches.stream().allMatch(RedisSwitchRow::enabled);
        }
    }
}
