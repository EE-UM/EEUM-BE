package com.eeum.global.securitycore.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eeum.domain.comment.service.CommentService;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.domain.web.controller.WebAuthController;
import com.eeum.domain.web.controller.WebCommentController;
import com.eeum.domain.web.controller.WebPostController;
import com.eeum.domain.web.controller.WebWriteController;
import com.eeum.domain.web.service.WebPostService;
import com.eeum.domain.web.service.WebUserService;
import com.eeum.global.securitycore.SecurityConfig;
import com.eeum.global.securitycore.admin.AdminOidcUserService;
import com.eeum.global.securitycore.admin.AdminSecurityConfig;
import com.eeum.global.securitycore.jwt.JWTUtil;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContext;
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
    WebSecurityConfigTest.TestConfig.class,
    WebSecurityConfig.class,
    WebOidcUserService.class,
    AdminSecurityConfig.class,
    AdminOidcUserService.class,
    SecurityConfig.class,
    WebPostController.class,
    WebAuthController.class,
    WebCommentController.class,
    WebWriteController.class
})
@TestPropertySource(properties = {
    "admin.kakao.client-id=test-client-id",
    "admin.kakao.client-secret=test-client-secret",
    "admin.allowed-kakao-ids=1234567890"
})
class WebSecurityConfigTest {

    @EnableWebMvc
    @EnableWebSecurity
    static class TestConfig {
    }

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private MessageService messageService;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private WebPostService webPostService;
    @MockitoBean
    private WebUserService webUserService;
    @MockitoBean
    private CommentService commentService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static OAuth2AuthenticationToken webAuthentication(boolean onboarded) {
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(),
            Instant.now().plusSeconds(60), Map.of("sub", "555"));
        WebPrincipal principal = new WebPrincipal(7L, onboarded ? "이음" : "", "kakao_555", null,
            onboarded, idToken);
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(),
            WebSecurityConfig.REGISTRATION_ID);
    }

    @Test
    @DisplayName("게시글 페이지는 비로그인으로 볼 수 있고 세션을 만들지 않는다")
    void postPage_isPublic() throws Exception {
        MvcResult result = mockMvc.perform(get("/p/1"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @DisplayName("비로그인으로 글쓰기에 들어가면 돌아올 경로를 담아 로그인으로 보낸다")
    void write_redirectsToLoginWithNext() throws Exception {
        mockMvc.perform(get("/w/write"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/w/login?next=/w/write"));
    }

    @Test
    @DisplayName("로그인 진입 시 next 를 세션에 담고 카카오 인가로 보낸다 (외부 주소는 저장하지 않는다)")
    void login_savesNextAndStartsKakao() throws Exception {
        MvcResult result = mockMvc.perform(get("/w/login").param("next", "/p/12"))
            .andExpect(redirectedUrl("/w/oauth2/authorization/kakao-web"))
            .andReturn();
        assertThat(result.getRequest().getSession().getAttribute(WebReturnTo.SESSION_KEY))
            .isEqualTo("/p/12");

        MvcResult evil = mockMvc.perform(get("/w/login").param("next", "//evil.com"))
            .andReturn();
        assertThat(evil.getRequest().getSession().getAttribute(WebReturnTo.SESSION_KEY)).isNull();
    }

    @Test
    @DisplayName("카카오 인가 요청은 /w 하위 콜백 주소를 쓴다")
    void kakaoAuthorization_usesWebCallback() throws Exception {
        MvcResult result = mockMvc.perform(get("/w/oauth2/authorization/kakao-web"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        assertThat(result.getResponse().getRedirectedUrl())
            .startsWith("https://kauth.kakao.com/oauth/authorize")
            .contains("scope=openid")
            .contains("redirect_uri=http://localhost/w/login/oauth2/code/kakao-web");
    }

    @Test
    @DisplayName("댓글 작성은 CSRF 토큰이 없으면 403 이고, 있으면 작성 후 게시글로 돌아간다")
    void comment_requiresCsrf() throws Exception {
        var request = post("/w/p/1/comments")
            .param("content", "같이 들어요")
            .param("music.albumName", "앨범")
            .param("music.songName", "곡")
            .param("music.artistName", "가수")
            .param("music.artworkUrl", "https://a/1.jpg")
            .param("music.appleMusicUrl", "https://p/1.m4a");

        mockMvc.perform(request.with(authentication(webAuthentication(true))))
            .andExpect(status().isForbidden());
        verify(commentService, never()).create(any(), any());

        mockMvc.perform(request.with(authentication(webAuthentication(true))).with(csrf()))
            .andExpect(redirectedUrl("/p/1?notice=commented"));
        verify(commentService).create(argThat(principal -> principal.getId().equals(7L)),
            argThat(req -> req.postId().equals(1L) && req.songName().equals("곡")
                && req.content().equals("같이 들어요")));
    }

    @Test
    @DisplayName("노래 없이 댓글을 보내면 저장하지 않고 안내한다")
    void comment_requiresMusic() throws Exception {
        mockMvc.perform(post("/w/p/1/comments").param("content", "노래 없음")
                .with(authentication(webAuthentication(true))).with(csrf()))
            .andExpect(redirectedUrl("/p/1?notice=music-required"));
        verify(commentService, never()).create(any(), any());
    }

    @Test
    @DisplayName("가입을 마치지 않은 사용자는 댓글 대신 가입 화면으로 간다")
    void comment_requiresSignup() throws Exception {
        mockMvc.perform(post("/w/p/1/comments").with(authentication(webAuthentication(false)))
                .with(csrf()))
            .andExpect(redirectedUrl("/w/signup"));
    }

    @Test
    @DisplayName("가입 완료 시 웹 전용 세션 키에 가입된 사용자로 저장하고 보던 곳으로 돌아간다")
    void signup_refreshesPrincipalInWebSessionKey() throws Exception {
        MvcResult login = mockMvc.perform(get("/w/login").param("next", "/p/3")).andReturn();
        HttpSession session = login.getRequest().getSession();

        MvcResult result = mockMvc.perform(post("/w/signup")
                .session((org.springframework.mock.web.MockHttpSession) session)
                .param("agreeTerms", "true").param("agreePrivacy", "true")
                .param("agreeCommunity", "true").param("nickname", " 새이음 ")
                .with(authentication(webAuthentication(false))).with(csrf()))
            .andExpect(redirectedUrl("/p/3"))
            .andReturn();

        verify(webUserService).completeSignup(eq(7L), eq("새이음"));
        SecurityContext saved = (SecurityContext) result.getRequest().getSession()
            .getAttribute(WebSecurityConfig.SECURITY_CONTEXT_KEY);
        WebPrincipal principal = (WebPrincipal) saved.getAuthentication().getPrincipal();
        assertThat(principal.isOnboarded()).isTrue();
        assertThat(principal.getNickname()).isEqualTo("새이음");
        // 관리자 체인이 읽는 기본 키에는 쓰지 않는다
        assertThat(result.getRequest().getSession().getAttribute("SPRING_SECURITY_CONTEXT")).isNull();
    }

    @Test
    @DisplayName("약관에 모두 동의하지 않으면 가입되지 않는다")
    void signup_requiresAllTerms() throws Exception {
        mockMvc.perform(post("/w/signup").param("agreeTerms", "true").param("nickname", "이음")
                .with(authentication(webAuthentication(false))).with(csrf()))
            .andExpect(status().isOk());
        verify(webUserService, never()).completeSignup(any(), any());
    }

    @Test
    @DisplayName("웹 체인은 관리자·앱 API 경로를 가로채지 않는다")
    void otherChains_areNotAffected() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(redirectedUrl("/admin/login"));
        MvcResult api = mockMvc.perform(get("/posts/infinite-scroll"))
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(api.getRequest().getSession(false)).isNull();
    }
}
