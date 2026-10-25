package com.eeum.domain.user.repository;

import com.eeum.domain.user.dto.response.GetProfileResponse;
import com.eeum.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByProviderId(String providerId);

    @Query(
        value = "select u.nickname, u.email " +
            "from users u " +
            "where u.id = :userId"
        , nativeQuery = true
    )
    Optional<GetProfileResponse> findProfileById(Long userId);

    boolean existsById(Long userId);

    @Query(value = """
            SELECT DATE_FORMAT(DATE_ADD(u.created_at, INTERVAL :offsetSeconds SECOND), '%Y-%m-%d')
                       AS signUpDate,
                   COUNT(*) AS signUpCount
            FROM users u
            WHERE u.created_at >= :from
            GROUP BY signUpDate
        """, nativeQuery = true)
    List<DailySignUpCount> countDailySignUps(@Param("from") LocalDateTime from,
        @Param("offsetSeconds") int offsetSeconds);
}
