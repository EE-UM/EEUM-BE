package com.eeum.domain.admin.service;

import com.eeum.domain.admin.dto.response.AdminDashboardResponse;
import com.eeum.domain.admin.dto.response.AdminDashboardResponse.DailySignUp;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.user.repository.DailySignUpCount;
import com.eeum.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static final int SIGN_UP_DAYS = 30;

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final Clock clock;

    @Autowired
    public AdminDashboardService(UserRepository userRepository, PostsRepository postsRepository) {
        this(userRepository, postsRepository, Clock.systemDefaultZone());
    }

    /**
     * @param clock created_at 이 저장될 때 사용된 JVM 시간대의 시계 (운영 컨테이너는 UTC, 로컬은 KST)
     */
    AdminDashboardService(UserRepository userRepository, PostsRepository postsRepository,
        Clock clock) {
        this.userRepository = userRepository;
        this.postsRepository = postsRepository;
        this.clock = clock;
    }

    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
            userRepository.count(),
            postsRepository.count(),
            postsRepository.countByIsCompletedTrue(),
            postsRepository.countDeletedByReason(DeleteReason.REPORT.name()),
            getDailySignUps()
        );
    }

    /**
     * 오늘을 포함한 최근 30일(KST)의 일별 가입 수. 가입이 없는 날은 0으로 채운다.
     */
    private List<DailySignUp> getDailySignUps() {
        Instant now = clock.instant();
        ZoneId storedZone = clock.getZone();
        LocalDate firstDay = LocalDate.ofInstant(now, KST).minusDays(SIGN_UP_DAYS - 1);

        LocalDateTime from = firstDay.atStartOfDay(KST)
            .withZoneSameInstant(storedZone)
            .toLocalDateTime();
        int offsetSeconds = KST.getRules().getOffset(now).getTotalSeconds()
            - storedZone.getRules().getOffset(now).getTotalSeconds();

        Map<LocalDate, Long> countByDate = userRepository.countDailySignUps(from, offsetSeconds)
            .stream()
            .collect(Collectors.toMap(row -> LocalDate.parse(row.getSignUpDate()),
                DailySignUpCount::getSignUpCount));

        return IntStream.range(0, SIGN_UP_DAYS)
            .mapToObj(firstDay::plusDays)
            .map(date -> new DailySignUp(date, countByDate.getOrDefault(date, 0L)))
            .toList();
    }
}
