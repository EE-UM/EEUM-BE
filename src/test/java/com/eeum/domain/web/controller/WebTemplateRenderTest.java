package com.eeum.domain.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.eeum.domain.web.dto.WebPostView;
import com.eeum.domain.web.dto.WebPostView.WebCommentView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * 공개 웹 템플릿이 실제로 렌더링되는지(문법 오류, 이스케이프, 상대 경로) 확인한다.
 * 환경 변수 {@code WEB_RENDER_OUT=<디렉터리>} 를 주면 렌더링 결과를 파일로 남겨 브라우저로 확인할 수 있다.
 */
class WebTemplateRenderTest {

    private final TemplateEngine engine = templateEngine();

    @Test
    void 게시글_페이지를_렌더링한다() throws IOException {
        WebPostView post = new WebPostView(1L, "비 오는 날 듣고 싶은 노래",
            "창밖에 비가 와요.\n<script>alert(1)</script> 같이 들을 노래 추천해 주세요.",
            "Rain", "Artist", null, "https://p/post.m4a", "창밖에 비가 와요.",
            List.of(new WebCommentView("Song A", "Singer A", null, "https://p/a.m4a", "이 노래 어때요"),
                new WebCommentView("Song B", null, null, null, null)));

        String html = render("web/post", Map.of("base", "../", "post", post));

        assertThat(html).contains("<title>비 오는 날 듣고 싶은 노래 · 이음</title>");
        assertThat(html).contains("href=\"../web/web.css\"");
        assertThat(html).contains("data-src=\"https://p/a.m4a\"");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;");
        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains("아티스트 미상");
        write("post.html", html);
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
