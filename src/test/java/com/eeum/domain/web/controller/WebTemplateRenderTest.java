package com.eeum.domain.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.eeum.domain.web.dto.WebCommentForm;
import com.eeum.domain.web.dto.WebMusic;
import com.eeum.domain.web.dto.WebPostView;
import com.eeum.domain.web.dto.WebPostView.WebCommentView;
import com.eeum.domain.web.dto.WebSignupForm;
import com.eeum.domain.web.dto.WebWriteForm;
import com.eeum.global.securitycore.web.WebPrincipal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * 공개 웹 템플릿이 실제로 렌더링되는지(문법 오류, 이스케이프, 상대 경로, 로그인 상태별 하단 바) 확인한다.
 * 환경 변수 {@code WEB_RENDER_OUT=<디렉터리>} 를 주면 렌더링 결과를 파일로 남겨 브라우저로 확인할 수 있다.
 */
class WebTemplateRenderTest {

    private final TemplateEngine engine = templateEngine();

    private static WebPostView post(boolean completed) {
        return new WebPostView(1L, "비 오는 날 듣고 싶은 노래",
            "창밖에 비가 와요.\n<script>alert(1)</script> 같이 들을 노래 추천해 주세요.",
            "Rain", "Artist", null, "https://p/post.m4a", "창밖에 비가 와요.", completed,
            List.of(new WebCommentView("Song A", "Singer A", null, "https://p/a.m4a", "이 노래 어때요"),
                new WebCommentView("Song B", null, null, null, null)));
    }

    private static WebPrincipal viewer(boolean onboarded) {
        OidcIdToken idToken = new OidcIdToken("token", Instant.now(),
            Instant.now().plusSeconds(60), Map.of("sub", "1"));
        return new WebPrincipal(7L, "이음", "kakao_1", null, onboarded, idToken);
    }

    private Map<String, Object> postModel(WebPrincipal viewer, boolean completed) {
        Map<String, Object> model = new HashMap<>();
        model.put("base", "../");
        model.put("post", post(completed));
        model.put("viewer", viewer);
        model.put("loginNext", "/p/1");
        model.put("commentForm", new WebCommentForm("", WebMusic.empty()));
        model.put("contentMaxLength", 200);
        return model;
    }

    @Test
    void 비로그인_게시글_페이지는_로그인_안내를_보여준다() throws IOException {
        String html = render("web/post", postModel(null, false));

        assertThat(html).contains("<title>비 오는 날 듣고 싶은 노래 · 이음</title>");
        assertThat(html).contains("href=\"../web/web.css\"");
        assertThat(html).contains("data-src=\"https://p/a.m4a\"");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;");
        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains("아티스트 미상");
        assertThat(html).contains("href=\"../w/login?next=/p/1\"");
        assertThat(html).doesNotContain("comment-bar");
        write("post.html", html);
    }

    @Test
    void 가입한_사용자에게는_댓글_입력과_노래_검색을_보여준다() throws IOException {
        String html = render("web/post", postModel(viewer(true), false));

        assertThat(html).contains("action=\"../w/p/1/comments\"");
        assertThat(html).contains("name=\"music.songName\"");
        assertThat(html).contains("data-search-url=\"../apple-music/search\"");
        assertThat(html).contains("action=\"../w/logout\"");
        write("post-login.html", html);
    }

    @Test
    void 완성된_사연에는_댓글_입력을_보여주지_않는다() {
        String html = render("web/post", postModel(viewer(true), true));

        assertThat(html).contains("플레이리스트가 완성된 사연이에요.");
        assertThat(html).doesNotContain("comment-bar");
    }

    @Test
    void 가입_전_사용자는_가입으로_안내한다() {
        String html = render("web/post", postModel(viewer(false), false));

        assertThat(html).contains("href=\"../w/signup\"");
        assertThat(html).doesNotContain("comment-bar");
    }

    @Test
    void 가입_페이지를_렌더링한다() throws IOException {
        String html = render("web/signup", Map.of("base", "../", "form", WebSignupForm.empty(),
            "termsUrl", "https://t", "privacyUrl", "https://p", "nicknameMaxLength", 30,
            "error", "필수 약관에 모두 동의해 주세요."));

        assertThat(html).contains("name=\"agreeCommunity\"");
        assertThat(html).contains("제3조 (금지되는 콘텐츠 및 행위)");
        assertThat(html).contains("필수 약관에 모두 동의해 주세요.");
        write("signup.html", html);
    }

    @Test
    void 글쓰기_페이지를_렌더링한다() throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("base", "../");
        model.put("viewer", viewer(true));
        model.put("form", WebWriteForm.empty());
        model.put("limitOptions", WebWriteForm.LIMIT_OPTIONS);
        model.put("titleMaxLength", 50);
        model.put("contentMaxLength", 200);
        String html = render("web/write", model);

        assertThat(html).contains("action=\"../w/write\"");
        assertThat(html).contains("value=\"AUTO_COMPLETION\" checked=\"checked\"");
        assertThat(html.replaceAll("\\s+", " ")).contains("<option value=\"20\" selected=\"selected\">20개");
        write("write.html", html);
    }

    @Test
    void 안내_페이지를_렌더링한다() throws IOException {
        String html = render("web/message",
            Map.of("base", "./", "heading", "아직 도착한 사연이 없어요", "message", "조금 뒤에 다시 들러 주세요."));

        assertThat(html).contains("아직 도착한 사연이 없어요");
        assertThat(html).contains("src=\"./web/img/nodata.png\"");
        write("message.html", html);
    }

    private String render(String template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return engine.process(template, context);
    }

    private static void write(String fileName, String html) throws IOException {
        String out = System.getenv("WEB_RENDER_OUT");
        if (out != null) {
            Files.writeString(Path.of(out, fileName), html);
        }
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
