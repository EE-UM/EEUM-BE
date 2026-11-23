package com.eeum.global.securitycore.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

/**
 * 공개 웹(eeum.site) 전용 SecurityFilterChain. 화면(/, /p/**, /web/**)은 누구나 보고, /w/** 는 카카오 OIDC 로그인 + 세션 + CSRF.
 * <ul>
 *   <li>관리자 체인(@Order(1))과 같은 카카오 앱(admin.kakao.*)을 재사용하고 등록 id·콜백 경로만 다르다.</li>
 *   <li>관리자와 같은 HttpSession 을 쓰므로 SecurityContext 를 별도 세션 키에 저장해 두 로그인이 서로를 덮어쓰지 않게 한다.</li>
 *   <li>앱 API 체인(SecurityConfig, @Order(3))보다 먼저 매칭된다.</li>
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    public static final String REGISTRATION_ID = "kakao-web";
    public static final String SECURITY_CONTEXT_KEY = "EEUM_WEB_SECURITY_CONTEXT";
    static final String LOGIN_PATH = "/w/login";
    static final String LOGIN_ERROR_PATH = "/w/login/error";

    private final WebOidcUserService webOidcUserService;

    @Value("${admin.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${admin.kakao.client-secret:}")
    private String kakaoClientSecret;

    /**
     * 웹 로그인 정보를 담는 세션 저장소. 가입 완료 후 principal 을 갱신할 때도 같은 키로 저장해야 하므로 공유한다.
     */
    public static HttpSessionSecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setSpringSecurityContextKey(SECURITY_CONTEXT_KEY);
        return repository;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/", "/p/**", "/w/**", "/web/**");
        http.securityContext(context -> context.securityContextRepository(securityContextRepository()));

        if (!StringUtils.hasText(kakaoClientId)) {
            log.warn("[WebSecurity] admin.kakao.client-id 미설정 - 웹 로그인·쓰기(/w/**)를 거부합니다.");
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/w/**").denyAll()
                .anyRequest().permitAll());
            return http.build();
        }

        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/p/**", "/web/**", LOGIN_PATH, LOGIN_ERROR_PATH).permitAll()
            .anyRequest().authenticated()
        );
        http.oauth2Login(oauth2 -> oauth2
            .loginPage(LOGIN_PATH)
            .clientRegistrationRepository(
                new InMemoryClientRegistrationRepository(kakaoRegistration()))
            .authorizedClientRepository(new HttpSessionOAuth2AuthorizedClientRepository())
            .authorizationEndpoint(endpoint -> endpoint.baseUri("/w/oauth2/authorization"))
            .redirectionEndpoint(endpoint -> endpoint.baseUri("/w/login/oauth2/code/*"))
            .userInfoEndpoint(userInfo -> userInfo.oidcUserService(webOidcUserService))
            .successHandler(new WebLoginSuccessHandler())
            .failureUrl(LOGIN_ERROR_PATH)
        );
        // 보던 페이지(GET)로 돌아올 수 있게 로그인 화면에 next 를 넘긴다
        http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
            (request, response, e) -> redirectStrategy.sendRedirect(request, response,
                loginPathFor(request))));
        // 같은 세션의 관리자 로그인은 유지한다
        http.logout(logout -> logout
            .logoutUrl("/w/logout")
            .invalidateHttpSession(false)
            .logoutSuccessUrl("/")
        );

        return http.build();
    }

    static String loginPathFor(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        if ("GET".equals(request.getMethod()) && WebReturnTo.isAllowed(path)) {
            return LOGIN_PATH + "?next=" + path;
        }
        return LOGIN_PATH;
    }

    /**
     * 컨텍스트 경로(ForwardedHeaderFilter 가 반영한 /dev 접두어 포함)를 뺀 요청 경로.
     */
    public static String pathWithinApplication(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private ClientRegistration kakaoRegistration() {
        return ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientId(kakaoClientId)
            .clientSecret(kakaoClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/w/login/oauth2/code/{registrationId}")
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
