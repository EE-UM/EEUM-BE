package com.eeum.global.securitycore.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.eeum.domain.admin.controller.AdminController;
import com.eeum.domain.admin.controller.AdminPostController;
import com.eeum.domain.admin.service.AdminDashboardService;
import com.eeum.domain.admin.service.AdminPostService;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.SecurityConfig;
import com.eeum.global.securitycore.jwt.JWTUtil;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * nginx 의 /dev/admin location 이 보내는 헤더를 그대로 흉내 내어, 앱이 /dev 접두어·https·실제 IP 를 올바르게 반영하는지 검증한다.
 */
@SpringJUnitWebConfig(classes = {
    AdminSecurityConfigTest.TestConfig.class,
    AdminSecurityConfig.class,
    AdminOidcUserService.class,
    AdminWebConfig.class,
    SecurityConfig.class,
    AdminController.class,
    AdminPostController.class
})
@TestPropertySource(properties = {
    "admin.kakao.client-id=test-client-id",
    "admin.kakao.client-secret=test-client-secret",
    "admin.allowed-kakao-ids=1234567890"
})
class AdminForwardedHeaderTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @MockitoBean
    private AdminPostService adminPostService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterRegistrationBean<ForwardedHeaderFilter> adminForwardedHeaderFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilter(adminForwardedHeaderFilter.getFilter(), "/admin/*")
            .apply(springSecurity())
            .build();
    }

    private MockHttpServletRequestBuilder viaNginxDev(MockHttpServletRequestBuilder request) {
        return request
            .header("Host", "eeum.site")
            .header("X-Forwarded-Proto", "https")
            .header("X-Forwarded-Host", "eeum.site")
            .header("X-Forwarded-Port", "443")
            .header("X-Forwarded-Prefix", "/dev")
            .header("X-Forwarded-For", "203.0.113.7");
    }

    private OAuth2AuthenticationToken adminAuthentication() {
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(),
            Instant.now().plusSeconds(60), Map.of("sub", "1234567890"));
        AdminPrincipal admin = new AdminPrincipal(42L, "관리자",
            List.of(new SimpleGrantedAuthority(AdminOidcUserService.ROLE_ADMIN)), idToken);
        return new OAuth2AuthenticationToken(admin, admin.getAuthorities(),
            AdminSecurityConfig.REGISTRATION_ID);
    }

    @Test
    @DisplayName("ForwardedHeaderFilter 는 /admin/* 에만, Spring Security 보다 먼저 등록된다")
    void filterRegistration_isScopedToAdmin() {
        assertThat(adminForwardedHeaderFilter.getUrlPatterns()).containsExactly("/admin/*");
        assertThat(adminForwardedHeaderFilter.getOrder()).isLessThan(0);
    }

    @Test
    @DisplayName("/dev 로 들어온 비로그인 요청은 https://eeum.site/dev/admin/login 으로 리다이렉트된다")
    void devPrefix_isKeptOnLoginRedirect() throws Exception {
        mockMvc.perform(viaNginxDev(get("/admin")))
            .andExpect(redirectedUrl("https://eeum.site/dev/admin/login"));
    }

    @Test
    @DisplayName("카카오 콜백 주소는 https 와 /dev 접두어를 포함한다")
    void kakaoRedirectUri_usesHttpsAndDevPrefix() throws Exception {
        String location = mockMvc.perform(
                viaNginxDev(get("/admin/oauth2/authorization/kakao-admin")))
            .andReturn().getResponse().getRedirectedUrl();

        assertThat(location).contains(
            "redirect_uri=https://eeum.site/dev/admin/login/oauth2/code/kakao-admin");
    }

    @Test
    @DisplayName("관리자 조치 후 리다이렉트에 /dev 접두어가 붙고, 감사 로그 IP 는 X-Forwarded-For 의 실제 접속 IP 다")
    void adminAction_keepsPrefixAndRecordsClientIp() throws Exception {
        mockMvc.perform(viaNginxDev(post("/admin/posts/100/delete"))
                .param("reason", "스팸")
                .with(authentication(adminAuthentication()))
                .with(csrf()))
            .andExpect(redirectedUrl("https://eeum.site/dev/admin/posts/100"));

        verify(adminPostService).delete(eq(100L), eq(42L), eq("스팸"), eq("203.0.113.7"));
    }

    @Test
    @DisplayName("세션 쿠키 이름은 프로파일별로 분리된다 (dev/prod 가 같은 도메인을 공유)")
    void sessionCookieName_isPerProfile() {
        assertThat(AdminWebConfig.sessionCookieName(new String[]{"dev"}))
            .isEqualTo("EEUM_ADMIN_SESSION_DEV");
        assertThat(AdminWebConfig.sessionCookieName(new String[]{"prod"}))
            .isEqualTo("EEUM_ADMIN_SESSION_PROD");
        assertThat(AdminWebConfig.sessionCookieName(new String[]{}))
            .isEqualTo("EEUM_ADMIN_SESSION");
    }
}
