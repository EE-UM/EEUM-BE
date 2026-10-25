package com.eeum.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eeum.domain.admin.dto.response.AdminDashboardResponse;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailySignUp;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.user.repository.DailySignUpCount;
import com.eeum.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    // 2026-10-25 01:00 KST == 2026-10-24 16:00 UTC
    private static final Instant NOW = Instant.parse("2026-10-24T16:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostsRepository postsRepository;

    private static DailySignUpCount row(String date, long count) {
        return new DailySignUpCount() {
            @Override
            public String getSignUpDate() {
                return date;
            }

            @Override
            public Long getSignUpCount() {
                return count;
            }
        };
    }

    private AdminDashboardService serviceWithClockIn(ZoneId zone) {
        return new AdminDashboardService(userRepository, postsRepository,
            Clock.fixed(NOW, zone));
    }

    @Test
    @DisplayName("집계 값들을 그대로 담고, 최근 30일(KST)을 빠짐없이 0으로 채운다")
    void getDashboard_fillsMissingDaysWithZero() {
        given(userRepository.count()).willReturn(120L);
        given(postsRepository.count()).willReturn(80L);
        given(postsRepository.countByIsCompletedTrue()).willReturn(30L);
        given(postsRepository.countDeletedByReason("REPORT")).willReturn(4L);
        given(userRepository.countDailySignUps(any(), anyInt())).willReturn(List.of(
            row("2026-10-25", 3L),
            row("2026-09-26", 1L)
        ));

        AdminDashboardResponse response = serviceWithClockIn(ZoneOffset.UTC).getDashboard();

        assertThat(response.totalUsers()).isEqualTo(120L);
        assertThat(response.totalPosts()).isEqualTo(80L);
        assertThat(response.completedPosts()).isEqualTo(30L);
        assertThat(response.reportDeletedPosts()).isEqualTo(4L);

        List<DailySignUp> days = response.dailySignUps();
        assertThat(days).hasSize(30);
        assertThat(days.getFirst()).isEqualTo(new DailySignUp(LocalDate.of(2026, 9, 26), 1L));
        assertThat(days.getLast()).isEqualTo(new DailySignUp(LocalDate.of(2026, 10, 25), 3L));
        assertThat(days.get(10).count()).isZero();
        assertThat(response.signUpsInPeriod()).isEqualTo(4L);
    }

    @Test
    @DisplayName("created_at 이 UTC 로 저장되는 환경(운영 컨테이너)에서는 KST 자정을 UTC 로 바꾼 시각부터, +9시간 보정해 집계한다")
    void dailySignUps_utcStoredZone_shiftsToKst() {
        serviceWithClockIn(ZoneOffset.UTC).getDashboard();

        // KST 2026-09-26 00:00 == UTC 2026-09-25 15:00
        verify(userRepository).countDailySignUps(LocalDateTime.of(2026, 9, 25, 15, 0), 9 * 3600);
    }

    @Test
    @DisplayName("created_at 이 KST 로 저장되는 환경(로컬)에서는 보정 없이 KST 자정부터 집계한다")
    void dailySignUps_kstStoredZone_noShift() {
        serviceWithClockIn(AdminDashboardService.KST).getDashboard();

        verify(userRepository).countDailySignUps(LocalDateTime.of(2026, 9, 26, 0, 0), 0);
    }

    @Test
    @DisplayName("차트 최댓값은 일별 최대 가입 수 이상인 1·2·5 단위 값이고, 가입이 없으면 1이다")
    void chartMax_roundsUpToNiceNumber() {
        assertThat(responseWithMax(0).chartMax()).isEqualTo(1L);
        assertThat(responseWithMax(1).chartMax()).isEqualTo(1L);
        assertThat(responseWithMax(3).chartMax()).isEqualTo(5L);
        assertThat(responseWithMax(7).chartMax()).isEqualTo(10L);
        assertThat(responseWithMax(12).chartMax()).isEqualTo(20L);
        assertThat(responseWithMax(51).chartMax()).isEqualTo(100L);

        AdminDashboardResponse response = responseWithMax(3);
        assertThat(response.barHeightPercent(new DailySignUp(LocalDate.now(), 3L))).isEqualTo(60);
    }

    private AdminDashboardResponse responseWithMax(long max) {
        return new AdminDashboardResponse(0, 0, 0, 0, List.of(
            new DailySignUp(LocalDate.of(2026, 10, 24), 0L),
            new DailySignUp(LocalDate.of(2026, 10, 25), max)
        ));
    }
}
