package com.eeum.global.securitycore.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminOidcUserServiceTest {

    private static final String ADMIN_KAKAO_ID = "1234567890";

    @Mock
    private OAuth2UserService<OidcUserRequest, OidcUser> delegate;

    @Mock
    private UserRepository userRepository;

    private final OidcUserRequest request = mock(OidcUserRequest.class);

    private AdminOidcUserService adminOidcUserService;

    @BeforeEach
    void setUp() {
        adminOidcUserService = new AdminOidcUserService(delegate, userRepository,
            " " + ADMIN_KAKAO_ID + " , 555");
    }

    private OidcUser kakaoUser(String sub) {
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(),
            Instant.now().plusSeconds(60), Map.of("sub", sub));
        return new DefaultOidcUser(List.of(), idToken, "sub");
    }

    private User registeredUser(Long id) {
        User user = User.of("관리자", "kakao_567890", "admin@test.com", "USER", "KAKAO",
            ADMIN_KAKAO_ID, true);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("허용 목록에 있는 카카오 계정은 ROLE_ADMIN 과 users.id 를 가진 AdminPrincipal 로 인증된다")
    void allowedKakaoUser_isAuthenticatedAsAdmin() {
        given(delegate.loadUser(request)).willReturn(kakaoUser(ADMIN_KAKAO_ID));
        given(userRepository.findByProviderAndProviderId("KAKAO", ADMIN_KAKAO_ID))
            .willReturn(Optional.of(registeredUser(42L)));

        OidcUser result = adminOidcUserService.loadUser(request);

        assertThat(result).isInstanceOf(AdminPrincipal.class);
        AdminPrincipal admin = (AdminPrincipal) result;
        assertThat(admin.getUserId()).isEqualTo(42L);
        assertThat(admin.getNickname()).isEqualTo("관리자");
        assertThat(admin.getAuthorities()).extracting(GrantedAuthority::getAuthority)
            .containsExactly(AdminOidcUserService.ROLE_ADMIN);
    }

    @Test
    @DisplayName("허용 목록에 없는 카카오 계정은 users 조회 없이 거부된다")
    void notAllowedKakaoUser_isRejected() {
        given(delegate.loadUser(request)).willReturn(kakaoUser("999"));

        assertThatThrownBy(() -> adminOidcUserService.loadUser(request))
            .isInstanceOf(OAuth2AuthenticationException.class);

        verify(userRepository, never()).findByProviderAndProviderId(any(), any());
    }

    @Test
    @DisplayName("허용 목록에 있어도 가입된 유저가 아니면 거부된다")
    void allowedButNotRegistered_isRejected() {
        given(delegate.loadUser(request)).willReturn(kakaoUser(ADMIN_KAKAO_ID));
        given(userRepository.findByProviderAndProviderId("KAKAO", ADMIN_KAKAO_ID))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> adminOidcUserService.loadUser(request))
            .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("허용 목록이 비어 있으면 모든 계정이 거부된다")
    void emptyAllowList_rejectsEveryone() {
        AdminOidcUserService noAdmins = new AdminOidcUserService(delegate, userRepository, "");
        given(delegate.loadUser(request)).willReturn(kakaoUser(""));

        assertThatThrownBy(() -> noAdmins.loadUser(request))
            .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
