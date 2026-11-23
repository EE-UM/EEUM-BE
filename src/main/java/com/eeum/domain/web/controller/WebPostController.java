package com.eeum.domain.web.controller;

import com.eeum.domain.web.dto.WebCommentForm;
import com.eeum.domain.web.dto.WebMusic;
import com.eeum.domain.web.service.WebPostService;
import com.eeum.global.securitycore.web.WebPrincipal;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * eeum.site 공개 웹 페이지. 보기는 누구나, 댓글은 카카오 로그인 후 가능하다.
 * <p>
 * dev 는 nginx 가 /dev 접두어를 떼고 넘기므로(eeum.site/dev/p/1 → /p/1) 리다이렉트·정적 리소스 경로는 모두 상대 경로로 둔다.
 * 템플릿의 {@code base} 는 현재 페이지에서 웹 루트까지의 상대 경로다.
 */
@Hidden
@Controller
@RequiredArgsConstructor
public class WebPostController {

    static final String ROOT_BASE = "./";
    static final String POST_BASE = "../";

    /**
     * 댓글·글쓰기 결과 안내. 플래시 속성은 dev(/dev 접두어)에서 대상 경로가 어긋나 유실되므로 고정 코드를 쿼리로 넘긴다.
     */
    static final Map<String, String> NOTICES = Map.of(
        "commented", "노래를 남겼어요.",
        "posted", "사연을 공유했어요. 친구들이 어울리는 노래를 남겨 줄 거예요.",
        "finished", "플레이리스트가 이미 완성된 사연이에요.",
        "duplicate", "사연에 담긴 노래와 다른 노래를 골라 주세요.",
        "music-required", "노래를 골라 주세요.",
        "too-long", "글은 200자까지 쓸 수 있어요.",
        "failed", "노래를 남기지 못했어요. 잠시 후 다시 시도해 주세요."
    );

    private final WebPostService webPostService;

    /**
     * 루트 접속 시 진행 중인 사연 하나로 보낸다. 방문할 때마다 달라지도록 캐시하지 않는다.
     * <p>
     * {@code redirect:} / {@code sendRedirect} 는 Tomcat 이 백엔드가 받은 요청 기준의 절대 URL(http, /dev 접두어 없음)로
     * 바꿔 버리므로, Location 에 상대 경로를 직접 넣어 브라우저가 현재 주소(https, /dev/) 기준으로 해석하게 한다.
     */
    @GetMapping("/")
    public String randomPost(HttpServletResponse response, Model model,
        @AuthenticationPrincipal WebPrincipal viewer) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        Optional<Long> postId = webPostService.pickRandomPostId();
        if (postId.isPresent()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader(HttpHeaders.LOCATION, "p/" + postId.get());
            return null; // HttpServletResponse 를 인자로 받았으므로 null 반환 시 뷰를 렌더링하지 않는다
        }
        model.addAttribute("base", ROOT_BASE);
        model.addAttribute("viewer", viewer);
        model.addAttribute("heading", "아직 도착한 사연이 없어요");
        model.addAttribute("message", "조금 뒤에 다시 들러 주세요.");
        return "web/message";
    }

    @GetMapping("/p/{postId}")
    public String post(@PathVariable("postId") Long postId,
        @RequestParam(value = "notice", required = false) String notice,
        @AuthenticationPrincipal WebPrincipal viewer, Model model) {
        model.addAttribute("base", POST_BASE);
        model.addAttribute("viewer", viewer);
        model.addAttribute("post",
            webPostService.read(postId, viewer == null ? null : viewer.getUserId()));
        model.addAttribute("notice", notice == null ? null : NOTICES.get(notice));
        model.addAttribute("loginNext", "/p/" + postId);
        model.addAttribute("commentForm", new WebCommentForm("", WebMusic.empty()));
        model.addAttribute("contentMaxLength", WebCommentForm.CONTENT_MAX_LENGTH);
        return "web/post";
    }
}
