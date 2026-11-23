package com.eeum.domain.web.controller;

import com.eeum.domain.web.service.WebPostService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * eeum.site 공개 웹 페이지 (비로그인 읽기 전용).
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

    private final WebPostService webPostService;

    /**
     * 루트 접속 시 진행 중인 사연 하나로 보낸다. 방문할 때마다 달라지도록 캐시하지 않는다.
     * <p>
     * {@code redirect:} / {@code sendRedirect} 는 Tomcat 이 백엔드가 받은 요청 기준의 절대 URL(http, /dev 접두어 없음)로
     * 바꿔 버리므로, Location 에 상대 경로를 직접 넣어 브라우저가 현재 주소(https, /dev/) 기준으로 해석하게 한다.
     */
    @GetMapping("/")
    public String randomPost(HttpServletResponse response, Model model) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        Optional<Long> postId = webPostService.pickRandomPostId();
        if (postId.isPresent()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader(HttpHeaders.LOCATION, "p/" + postId.get());
            return null; // HttpServletResponse 를 인자로 받았으므로 null 반환 시 뷰를 렌더링하지 않는다
        }
        model.addAttribute("base", ROOT_BASE);
        model.addAttribute("heading", "아직 도착한 사연이 없어요");
        model.addAttribute("message", "조금 뒤에 다시 들러 주세요.");
        return "web/message";
    }

    @GetMapping("/p/{postId}")
    public String post(@PathVariable("postId") Long postId, Model model) {
        model.addAttribute("base", POST_BASE);
        model.addAttribute("post", webPostService.read(postId));
        return "web/post";
    }
}
