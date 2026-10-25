package com.eeum.global.securitycore.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eeum.domain.admin.controller.AdminController;
import com.eeum.domain.admin.controller.AdminPostController;
import com.eeum.domain.admin.service.AdminPostService;
import com.eeum.domain.admin.service.AdminDashboardService;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(classes = {
    AdminSecurityConfigTest.TestConfig.class,
    AdminSecurityConfig.class,
    AdminOidcUserService.class,
    SecurityConfig.class,
    AdminController.class,
    AdminPostController.class
})
@TestPropertySource(properties = {
    "admin.kakao.client-id=test-client-id",
    "admin.kakao.client-secret=test-client-secret",
    "admin.allowed-kakao-ids=1234567890"
})
class AdminSecurityConfigTest {

    @EnableWebMvc
    @EnableWebSecurity
    static class TestConfig {
    }

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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
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
    @DisplayName("비로그인 상태로 /admin 접근 시 관리자 로그인 페이지로 리다이렉트된다")
    void anonymous_redirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    @DisplayName("로그인 페이지와 스타일시트는 비로그인 상태로 접근할 수 있다")
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/admin/login")).andExpect(status().isOk());
        // 테스트 컨텍스트에는 정적 리소스 핸들러가 없으므로, 보안 필터를 통과했다면 리다이렉트가 아닌 404 가 된다.
        mockMvc.perform(get("/admin/admin.css")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("카카오 로그인 시작 시 openid scope 와 /admin 하위 콜백 주소로 카카오 인가 페이지에 리다이렉트된다")
    void kakaoAuthorization_redirectsToKakao() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/oauth2/authorization/kakao-admin"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertThat(location)
            .startsWith("https://kauth.kakao.com/oauth/authorize")
            .contains("client_id=test-client-id")
            .contains("scope=openid")
            .contains("nonce=")
            .contains("redirect_uri=http://localhost/admin/login/oauth2/code/kakao-admin");
    }

    @Test
    @DisplayName("ROLE_ADMIN 으로 인증된 관리자는 /admin 에 접근할 수 있다")
    void admin_canAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin").with(authentication(adminAuthentication())))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_ADMIN 이 아닌 인증 사용자는 /admin 접근이 거부된다")
    void nonAdmin_isForbidden() throws Exception {
        mockMvc.perform(get("/admin").with(user("someone").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF 토큰 없이 POST 하면 403, 토큰이 있으면 로그아웃된다")
    void logout_requiresCsrf() throws Exception {
        mockMvc.perform(post("/admin/logout").with(authentication(adminAuthentication())))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/logout").with(authentication(adminAuthentication()))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/login?logout"));
    }

    @Test
    @DisplayName("관리자 체인은 앱 API 경로에 영향을 주지 않는다 (리다이렉트·세션 생성 없음)")
    void apiPaths_areNotAffected() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/infinite-scroll"))
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @DisplayName("게시글 삭제 POST 는 CSRF 토큰이 없으면 403 이고 서비스가 호출되지 않는다")
    void postDelete_requiresCsrf() throws Exception {
        mockMvc.perform(post("/admin/posts/100/delete").param("reason", "스팸")
                .with(authentication(adminAuthentication())))
            .andExpect(status().isForbidden());

        verify(adminPostService, never()).delete(any(), any(), any(), any());

        mockMvc.perform(post("/admin/posts/100/delete").param("reason", "스팸")
                .with(authentication(adminAuthentication())).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/posts/100"));

        verify(adminPostService).delete(eq(100L), eq(42L), eq("스팸"), any());
    }
}
