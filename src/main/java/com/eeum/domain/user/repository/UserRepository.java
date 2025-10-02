package com.eeum.domain.user.repository;

import com.eeum.domain.user.dto.response.GetProfileResponse;
import com.eeum.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

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
}
