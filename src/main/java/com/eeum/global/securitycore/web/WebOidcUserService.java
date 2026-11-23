package com.eeum.global.securitycore.web;

import com.eeum.domain.common.constant.DiscordWebhookType;
import com.eeum.domain.common.webhook.discord.DiscordWebhookResponse;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.common.webhook.discord.message.MessageFormatter;
import com.eeum.domain.user.entity.Status;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.oidc.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 공개 웹 카카오 OIDC 로그인. 앱과 같은 카카오 앱을 쓰므로 sub 가 users.provider_id 와 같다. 처음 보는 계정이면 가입 전(isRegistered=false)
 * 상태로 만들고, 약관 동의·닉네임 입력(/w/signup)을 마쳐야 쓰기 기능을 쓸 수 있다.
 */
@Slf4j
@Component
public class WebOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final String environment;

    @Autowired
    public WebOidcUserService(UserRepository userRepository, MessageService messageService,
        @Value("${discord.environment:}") String environment) {
        this(new OidcUserService(), userRepository, messageService, environment);
    }

    WebOidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> delegate,
        UserRepository userRepository, MessageService messageService, String environment) {
        this.delegate = delegate;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.environment = environment;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String kakaoId = oidcUser.getSubject();

        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO.name(), kakaoId)
            .orElseGet(() -> createPendingUser(kakaoId, oidcUser.getEmail()));

        if (user.getStatus() == Status.BANNED || user.getStatus() == Status.SUSPENDED) {
            log.warn("[WebLogin] 이용 제한 계정 로그인 시도 - userId={}", user.getId());
            throw new OAuth2AuthenticationException(new OAuth2Error("account_restricted"),
                "Restricted account.");
        }

        log.info("[WebLogin] 웹 로그인 - userId={}", user.getId());
        return new WebPrincipal(user.getId(), user.getNickname(), user.getUsername(),
            user.getEmail(), isOnboarded(user), oidcUser.getIdToken());
    }

    static boolean isOnboarded(User user) {
        return user.isRegistered()
            && user.getStatus() != Status.DELETED
            && StringUtils.hasText(user.getNickname());
    }

    private User createPendingUser(String kakaoId, String email) {
        String tail = kakaoId.length() > 6 ? kakaoId.substring(kakaoId.length() - 6) : kakaoId;
        User user = userRepository.saveAndFlush(
            User.of("", "kakao_" + tail, email, "USER", Provider.KAKAO.name(), kakaoId, false));
        messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(
                MessageFormatter.formatSignUpMessageSocialLogin(user.getId(), "KAKAO_WEB",
                    environment)),
            DiscordWebhookType.SIGNUP);
        return user;
    }
}
