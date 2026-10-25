package com.eeum.global.securitycore.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.util.StringUtils;

/**
 * /admin/** 전용 SecurityFilterChain. 앱 API 의 JWT 체인과 분리된 카카오 OIDC 로그인 + 세션 + CSRF 기반 인증을 사용한다.
 * admin.kakao.client-id 가 설정되지 않은 환경에서는 /admin/** 전체를 거부한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    public static final String REGISTRATION_ID = "kakao-admin";
    private static final String LOGIN_PAGE = "/admin/login";

    private final AdminOidcUserService adminOidcUserService;

    @Value("${admin.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${admin.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**");

        if (!StringUtils.hasText(kakaoClientId)) {
            log.warn("[AdminSecurity] admin.kakao.client-id 미설정 - /admin/** 접근을 모두 거부합니다.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(LOGIN_PAGE, "/admin/admin.css").permitAll()
            .anyRequest().hasAuthority(AdminOidcUserService.ROLE_ADMIN)
        );
        http.oauth2Login(oauth2 -> oauth2
            .loginPage(LOGIN_PAGE)
            .clientRegistrationRepository(
                new InMemoryClientRegistrationRepository(kakaoRegistration()))
            .authorizedClientRepository(new HttpSessionOAuth2AuthorizedClientRepository())
            .authorizationEndpoint(endpoint -> endpoint.baseUri("/admin/oauth2/authorization"))
            .redirectionEndpoint(endpoint -> endpoint.baseUri("/admin/login/oauth2/code/*"))
            .userInfoEndpoint(userInfo -> userInfo.oidcUserService(adminOidcUserService))
            .defaultSuccessUrl("/admin", true)
            .failureUrl(LOGIN_PAGE + "?error")
        );
        http.logout(logout -> logout
            .logoutUrl("/admin/logout")
            .logoutSuccessUrl(LOGIN_PAGE + "?logout")
        );
        http.sessionManagement(session -> session
            .maximumSessions(1)
        );

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public CookieSameSiteSupplier adminSessionCookieSameSiteSupplier() {
        // 카카오 → 콜백 리다이렉트(cross-site top-level GET)에 세션 쿠키가 실려야 하므로 Strict 가 아닌 Lax
        return CookieSameSiteSupplier.ofLax().whenHasName("JSESSIONID");
    }

    private ClientRegistration kakaoRegistration() {
        return ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientId(kakaoClientId)
            .clientSecret(kakaoClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/admin/login/oauth2/code/{registrationId}")
            .scope("openid")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .jwkSetUri("https://kauth.kakao.com/.well-known/jwks.json")
            .issuerUri("https://kauth.kakao.com")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .clientName("Kakao")
            .build();
    }
}
