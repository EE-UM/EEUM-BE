package com.eeum.global.securitycore.admin;

import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.oidc.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * 카카오 OIDC 로그인 후, 허용 목록(admin.allowed-kakao-ids)에 있는 카카오 회원번호(sub)만 ROLE_ADMIN 으로 인증한다.
 */
@Slf4j
@Component
public class AdminOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;
    private final UserRepository userRepository;
    private final Set<String> allowedKakaoIds;

    @Autowired
    public AdminOidcUserService(UserRepository userRepository,
        @Value("${admin.allowed-kakao-ids:}") String allowedKakaoIds) {
        this(new OidcUserService(), userRepository, allowedKakaoIds);
    }

    AdminOidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> delegate,
        UserRepository userRepository, String allowedKakaoIds) {
        this.delegate = delegate;
        this.userRepository = userRepository;
        this.allowedKakaoIds = Arrays.stream(allowedKakaoIds.split(","))
            .map(String::trim)
            .filter(id -> !id.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String kakaoId = oidcUser.getSubject();

        if (!allowedKakaoIds.contains(kakaoId)) {
            log.warn("[AdminLogin] 허용되지 않은 카카오 계정 로그인 시도 - sub={}", kakaoId);
            throw new OAuth2AuthenticationException(new OAuth2Error("access_denied"),
                "Not an admin account.");
        }

        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO.name(), kakaoId)
            .orElseThrow(() -> {
                log.warn("[AdminLogin] 허용 목록에 있지만 가입되지 않은 카카오 계정 - sub={}", kakaoId);
                return new OAuth2AuthenticationException(new OAuth2Error("admin_user_not_found"),
                    "Admin account is not registered as a user.");
            });

        log.info("[AdminLogin] 관리자 로그인 - userId={}", user.getId());
        return new AdminPrincipal(user.getId(), user.getNickname(),
            List.of(new SimpleGrantedAuthority(ROLE_ADMIN)), oidcUser.getIdToken());
    }
}
