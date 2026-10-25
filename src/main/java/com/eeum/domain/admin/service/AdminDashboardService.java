package com.eeum.domain.admin.service;

import com.eeum.domain.admin.dto.response.AdminDashboardResponse;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailyCount;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailySeries;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.Operations;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.Reports;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.Share;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.TodayMetric;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.Totals;
import com.eeum.domain.admin.repository.AdminStatsRepository;
import com.eeum.domain.admin.repository.AdminStatsRepository.DailySource;
import com.eeum.domain.admin.support.KstPeriod;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    public static final int SERIES_DAYS = 30;
    private static final int TOP_LIMIT = 5;
    private static final int RECENT_LIMIT = 8;

    private final AdminStatsRepository statsRepository;
    private final KstPeriod period;

    @Autowired
    public AdminDashboardService(AdminStatsRepository statsRepository) {
        this(statsRepository, KstPeriod.systemDefault());
    }

    AdminDashboardService(AdminStatsRepository statsRepository, KstPeriod period) {
        this.statsRepository = statsRepository;
        this.period = period;
    }

    public AdminDashboardResponse getDashboard() {
        LocalDate today = period.today();

        DailySeries signUps = series(DailySource.USERS, today);
        DailySeries posts = series(DailySource.POSTS, today);
        DailySeries comments = series(DailySource.COMMENTS, today);
        DailySeries likes = series(DailySource.LIKES, today, 2);
        DailySeries reports = sum(series(DailySource.POST_REPORTS, today, 2),
            series(DailySource.COMMENT_REPORTS, today, 2));

        Map<String, Long> postsByState = statsRepository.countPostsByState();

        return new AdminDashboardResponse(
            today,
            List.of(
                todayMetric("신규 가입", signUps),
                todayMetric("새 게시글", posts),
                todayMetric("새 댓글", comments),
                todayMetric("좋아요", likes),
                todayMetric("신고", reports)
            ),
            totals(postsByState),
            signUps,
            posts,
            comments,
            Share.of(statsRepository.countUsersByProvider(), AdminDashboardService::providerLabel),
            Share.of(statsRepository.countUsersByStatus(), AdminDashboardService::userStatusLabel),
            Share.of(postsByState, AdminDashboardService::postStateLabel),
            Share.of(statsRepository.countActivePostsByCompletionType(),
                AdminDashboardService::completionTypeLabel),
            statsRepository.topPostsByComments(TOP_LIMIT),
            statsRepository.topPostsByLikes(TOP_LIMIT),
            statsRepository.topPostsByViews(TOP_LIMIT),
            new Reports(statsRepository.countPostReports(), statsRepository.countCommentReports(),
                statsRepository.recentReports(RECENT_LIMIT)),
            operations(),
            statsRepository.recentAdminActions(RECENT_LIMIT)
        );
    }

    private Totals totals(Map<String, Long> postsByState) {
        long inProgress = postsByState.getOrDefault("IN_PROGRESS", 0L);
        long completed = postsByState.getOrDefault("COMPLETED", 0L);
        long deleted = postsByState.entrySet().stream()
            .filter(e -> e.getKey().startsWith("DELETED_"))
            .mapToLong(Map.Entry::getValue)
            .sum();
        return new Totals(
            statsRepository.countUsers(),
            inProgress + completed,
            inProgress,
            completed,
            deleted,
            statsRepository.countActiveComments(),
            statsRepository.countLikes(),
            statsRepository.sumViews(),
            statsRepository.countActiveBlocks()
        );
    }

    private Operations operations() {
        Map<String, Long> notifications = new LinkedHashMap<>();
        Map<String, Long> raw = statsRepository.countNotificationsByStatusSince(
            period.storedStartOf(period.today().minusDays(6)));
        for (String status : List.of("SENT", "PENDING", "SENDING", "FAILED")) {
            notifications.put(status, raw.getOrDefault(status, 0L));
        }
        return new Operations(
            statsRepository.countDeviceTokens(),
            Share.of(notifications, AdminDashboardService::notificationStatusLabel),
            notifications.get("FAILED"),
            statsRepository.redisSwitches(),
            statsRepository.latestBatchJobs(),
            statsRepository.countFailedBatchJobsSince(period.storedHoursAgo(24))
        );
    }

    private DailySeries series(DailySource source, LocalDate today) {
        return series(source, today, SERIES_DAYS);
    }

    /**
     * 오늘을 포함한 최근 days 일(KST)의 일별 수. 값이 없는 날은 0으로 채운다.
     */
    private DailySeries series(DailySource source, LocalDate today, int days) {
        LocalDate firstDay = today.minusDays(days - 1);
        Map<LocalDate, Long> counts = statsRepository.dailyCounts(source,
            period.storedStartOf(firstDay), period.offsetSecondsToKst());
        return new DailySeries(IntStream.range(0, days)
            .mapToObj(firstDay::plusDays)
            .map(date -> new DailyCount(date, counts.getOrDefault(date, 0L)))
            .toList());
    }

    private static DailySeries sum(DailySeries a, DailySeries b) {
        return new DailySeries(IntStream.range(0, a.days().size())
            .mapToObj(i -> new DailyCount(a.days().get(i).date(),
                a.days().get(i).count() + b.days().get(i).count()))
            .toList());
    }

    private static TodayMetric todayMetric(String label, DailySeries series) {
        List<DailyCount> days = series.days();
        return new TodayMetric(label, days.getLast().count(), days.get(days.size() - 2).count());
    }

    static String providerLabel(String provider) {
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> "카카오";
            case "APPLE" -> "애플";
            case "DEVICE", "GUEST" -> "게스트";
            case "UNKNOWN" -> "알 수 없음";
            default -> provider;
        };
    }

    static String userStatusLabel(String status) {
        return switch (status) {
            case "ACTIVE" -> "정상";
            case "SUSPENDED" -> "정지";
            case "BANNED" -> "차단";
            case "DELETED" -> "탈퇴";
            case "UNKNOWN" -> "미설정";
            default -> status;
        };
    }

    static String postStateLabel(String state) {
        return switch (state) {
            case "IN_PROGRESS" -> "진행 중";
            case "COMPLETED" -> "완료";
            case "DELETED_USER" -> "삭제 · 작성자";
            case "DELETED_REPORT" -> "삭제 · 신고";
            case "DELETED_SPAM" -> "삭제 · 스팸 필터";
            case "DELETED_ADMIN" -> "삭제 · 관리자";
            case "DELETED_UNKNOWN" -> "삭제 · 사유 미기록";
            default -> state;
        };
    }

    static String completionTypeLabel(String type) {
        return switch (type) {
            case "AUTO_COMPLETION" -> "댓글 수 도달 시 자동 완료";
            case "MANUAL_COMPLETION" -> "작성자 수동 마감";
            default -> "미설정";
        };
    }

    static String notificationStatusLabel(String status) {
        return switch (status) {
            case "SENT" -> "발송 완료";
            case "PENDING" -> "대기";
            case "SENDING" -> "발송 중";
            case "FAILED" -> "실패";
            default -> status;
        };
    }
}
