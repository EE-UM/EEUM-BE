package com.eeum.global.securitycore.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.user.entity.Status;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

class WebOidcUserServiceTest {

    private static final String KAKAO_ID = "4300123456";

    private UserRepository userRepository;
    private MessageService messageService;
    private WebOidcUserService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        OAuth2UserService<OidcUserRequest, OidcUser> delegate = mock(OAuth2UserService.class);
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(),
            Instant.now().plusSeconds(60), Map.of("sub", KAKAO_ID));
        given(delegate.loadUser(any())).willReturn(new DefaultOidcUser(java.util.List.of(), idToken));
        userRepository = mock(UserRepository.class);
        messageService = mock(MessageService.class);
        service = new WebOidcUserService(delegate, userRepository, messageService, "test");
    }

    private static User user(long id, String nickname, boolean registered, Status status) {
        User user = User.of(nickname, "kakao_123456", null, "USER", "KAKAO", KAKAO_ID, registered);
        ReflectionTestUtils.setField(user, "id", id);
        user.updateStatus(status);
        return user;
    }

    @Test
    void 가입한_회원은_가입_완료_상태로_로그인한다() {
        given(userRepository.findByProviderAndProviderId("KAKAO", KAKAO_ID))
            .willReturn(Optional.of(user(1L, "이음", true, Status.ACTIVE)));

        WebPrincipal principal = (WebPrincipal) service.loadUser(mock(OidcUserRequest.class));

        assertThat(principal.getUserId()).isEqualTo(1L);
        assertThat(principal.isOnboarded()).isTrue();
        verify(messageService, never()).sendDiscordWebhookMessage(any(), any());
    }

    @Test
    void 처음_보는_계정은_가입_전_상태로_만들고_알린다() {
        given(userRepository.findByProviderAndProviderId("KAKAO", KAKAO_ID))
            .willReturn(Optional.empty());
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 9L);
            return saved;
        });

        WebPrincipal principal = (WebPrincipal) service.loadUser(mock(OidcUserRequest.class));

        assertThat(principal.getUserId()).isEqualTo(9L);
        assertThat(principal.isOnboarded()).isFalse();
        assertThat(principal.getUsername()).isEqualTo("kakao_123456");
        verify(userRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
            saved -> !saved.isRegistered() && "KAKAO".equals(saved.getProvider())
                && KAKAO_ID.equals(saved.getProviderId())));
        verify(messageService).sendDiscordWebhookMessage(any(), any());
    }

    @Test
    void 탈퇴했거나_닉네임이_없으면_가입을_다시_받는다() {
        assertThat(WebOidcUserService.isOnboarded(user(1L, "이음", true, Status.DELETED))).isFalse();
        assertThat(WebOidcUserService.isOnboarded(user(1L, "", true, Status.ACTIVE))).isFalse();
        assertThat(WebOidcUserService.isOnboarded(user(1L, "이음", false, Status.ACTIVE))).isFalse();
    }

    @Test
    void 이용_제한_계정은_로그인할_수_없다() {
        given(userRepository.findByProviderAndProviderId("KAKAO", KAKAO_ID))
            .willReturn(Optional.of(user(1L, "이음", true, Status.BANNED)));

        assertThatThrownBy(() -> service.loadUser(mock(OidcUserRequest.class)))
            .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
