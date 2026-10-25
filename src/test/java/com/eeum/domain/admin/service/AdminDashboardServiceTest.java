package com.eeum.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eeum.domain.admin.dto.response.AdminDashboardResponse;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailyCount;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailySeries;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.Share;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.TodayMetric;
import com.eeum.domain.admin.repository.AdminStatsRepository;
import com.eeum.domain.admin.repository.AdminStatsRepository.DailySource;
import com.eeum.domain.admin.support.KstPeriod;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDashboardServiceTest {

    // 2026-10-25 01:00 KST == 2026-10-24 16:00 UTC, 운영 컨테이너처럼 UTC 로 저장되는 환경
    private static final Instant NOW = Instant.parse("2026-10-24T16:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);

    @Mock
    private AdminStatsRepository stats;

    private AdminDashboardService service;

    @BeforeEach
    void setUp() {
        service = new AdminDashboardService(stats,
            new KstPeriod(Clock.fixed(NOW, ZoneOffset.UTC)));
        given(stats.dailyCounts(any(), any(), anyInt())).willReturn(Map.of());
        given(stats.countPostsByState()).willReturn(Map.of());
        given(stats.countNotificationsByStatusSince(any())).willReturn(Map.of());
    }

    private static Map<String, Long> ordered(Object... keyValues) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], ((Number) keyValues[i + 1]).longValue());
        }
        return map;
    }

    @Test
    @DisplayName("30일 추이는 KST 자정을 저장 시간대(UTC)로 바꾼 시각부터 +9시간 보정해 조회하고, 빈 날은 0으로 채운다")
    void series_queriesFromKstMidnightAndFillsGaps() {
        given(stats.dailyCounts(eq(DailySource.USERS), any(), anyInt())).willReturn(Map.of(
            TODAY, 3L,
            LocalDate.of(2026, 9, 26), 1L));

        AdminDashboardResponse response = service.getDashboard();

        // KST 2026-09-26 00:00 == UTC 2026-09-25 15:00
        verify(stats).dailyCounts(DailySource.USERS, LocalDateTime.of(2026, 9, 25, 15, 0), 9 * 3600);
        List<DailyCount> days = response.signUps().days();
        assertThat(days).hasSize(30);
        assertThat(days.getFirst()).isEqualTo(new DailyCount(LocalDate.of(2026, 9, 26), 1L));
        assertThat(days.getLast()).isEqualTo(new DailyCount(TODAY, 3L));
        assertThat(days.get(10).count()).isZero();
        assertThat(response.signUps().total()).isEqualTo(4L);
        assertThat(response.today()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("오늘 지표는 오늘·어제 값을 담고, 신고는 게시글·댓글 신고를 합친다")
    void todayMetrics_compareWithYesterday() {
        given(stats.dailyCounts(eq(DailySource.POSTS), any(), anyInt())).willReturn(Map.of(
            TODAY, 5L, TODAY.minusDays(1), 2L));
        given(stats.dailyCounts(eq(DailySource.POST_REPORTS), any(), anyInt())).willReturn(Map.of(
            TODAY, 1L));
        given(stats.dailyCounts(eq(DailySource.COMMENT_REPORTS), any(), anyInt())).willReturn(
            Map.of(TODAY, 2L, TODAY.minusDays(1), 4L));

        List<TodayMetric> metrics = service.getDashboard().todayMetrics();

        assertThat(metrics).extracting(TodayMetric::label)
            .containsExactly("신규 가입", "새 게시글", "새 댓글", "좋아요", "신고");
        TodayMetric posts = metrics.get(1);
        assertThat(posts.today()).isEqualTo(5L);
        assertThat(posts.deltaText()).isEqualTo("어제보다 3 ▲");
        TodayMetric reports = metrics.get(4);
        assertThat(reports.today()).isEqualTo(3L);
        assertThat(reports.yesterday()).isEqualTo(4L);
        assertThat(reports.deltaText()).isEqualTo("어제보다 1 ▼");
        assertThat(metrics.getFirst().deltaText()).isEqualTo("어제와 같아요");
    }

    @Test
    @DisplayName("전체 현황은 게시글 상태 집계로 진행 중·완료·삭제 수를 계산한다")
    void totals_areDerivedFromPostStates() {
        given(stats.countPostsByState()).willReturn(ordered(
            "IN_PROGRESS", 7, "COMPLETED", 3, "DELETED_REPORT", 2, "DELETED_USER", 1));
        given(stats.countUsers()).willReturn(20L);

        var totals = service.getDashboard().totals();

        assertThat(totals.users()).isEqualTo(20L);
        assertThat(totals.activePosts()).isEqualTo(10L);
        assertThat(totals.inProgressPosts()).isEqualTo(7L);
        assertThat(totals.completedPosts()).isEqualTo(3L);
        assertThat(totals.deletedPosts()).isEqualTo(3L);
    }

    @Test
    @DisplayName("구성 비율은 한글 라벨과 전체 대비 %로 변환되고, 모르는 값은 원래 값을 보여준다")
    void shares_areLabeledWithPercent() {
        given(stats.countUsersByProvider()).willReturn(ordered("KAKAO", 3, "APPLE", 1));
        given(stats.countPostsByState()).willReturn(ordered("IN_PROGRESS", 1, "DELETED_SPAM", 1));

        AdminDashboardResponse response = service.getDashboard();

        assertThat(response.usersByProvider()).containsExactly(
            new Share("카카오", 3, 75), new Share("애플", 1, 25));
        assertThat(response.postsByState()).extracting(Share::label)
            .containsExactly("진행 중", "삭제 · 스팸 필터");
        assertThat(AdminDashboardService.providerLabel("idToken")).isEqualTo("idToken");
        assertThat(AdminDashboardService.userStatusLabel("UNKNOWN")).isEqualTo("미설정");
    }

    @Test
    @DisplayName("알림은 최근 7일(KST) 상태별 수를 고정 순서로 0 포함해 보여주고, 실패 수를 따로 낸다")
    void notifications_areOrderedWithZeros() {
        given(stats.countNotificationsByStatusSince(any())).willReturn(Map.of(
            "SENT", 10L, "FAILED", 2L));

        var operations = service.getDashboard().operations();

        // KST 2026-10-19 00:00 == UTC 2026-10-18 15:00
        verify(stats).countNotificationsByStatusSince(LocalDateTime.of(2026, 10, 18, 15, 0));
        assertThat(operations.notificationsLast7Days()).extracting(Share::label, Share::count)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("발송 완료", 10L),
                org.assertj.core.groups.Tuple.tuple("대기", 0L),
                org.assertj.core.groups.Tuple.tuple("발송 중", 0L),
                org.assertj.core.groups.Tuple.tuple("실패", 2L));
        assertThat(operations.failedNotificationsLast7Days()).isEqualTo(2L);
        verify(stats).countFailedBatchJobsSince(LocalDateTime.of(2026, 10, 23, 16, 0));
    }

    @Test
    @DisplayName("차트 최댓값은 일별 최대 이상인 1·2·5 단위 값이고, 값이 없으면 1이다")
    void chartMax_roundsUpToNiceNumber() {
        assertThat(seriesWithMax(0).chartMax()).isEqualTo(1L);
        assertThat(seriesWithMax(3).chartMax()).isEqualTo(5L);
        assertThat(seriesWithMax(7).chartMax()).isEqualTo(10L);
        assertThat(seriesWithMax(12).chartMax()).isEqualTo(20L);
        assertThat(seriesWithMax(51).chartMax()).isEqualTo(100L);
        assertThat(seriesWithMax(3).barHeightPercent(new DailyCount(TODAY, 3L))).isEqualTo(60);
    }

    @Test
    @DisplayName("오늘 날짜는 한국어 요일과 함께 표시된다")
    void todayLabel_isKorean() {
        assertThat(service.getDashboard().todayLabel()).isEqualTo("10월 25일 일요일");
    }

    private DailySeries seriesWithMax(long max) {
        return new DailySeries(List.of(
            new DailyCount(TODAY.minusDays(1), 0L),
            new DailyCount(TODAY, max)));
    }
}
