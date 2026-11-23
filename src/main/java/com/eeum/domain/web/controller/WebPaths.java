package com.eeum.domain.web.controller;

/**
 * 템플릿의 {@code base}(현재 페이지에서 웹 루트까지의 상대 경로) 계산. dev 는 nginx 가 /dev 접두어를 떼고 넘기므로 정적 리소스·링크를
 * 상대 경로로 둬야 /dev 안에 머문다.
 */
final class WebPaths {

    private WebPaths() {
    }

    /**
     * "/" → "./", "/p/1" → "../", "/w/p/1/comments" → "../../../"
     */
    static String baseOf(String servletPath) {
        int depth = (int) servletPath.chars().filter(c -> c == '/').count() - 1;
        return depth <= 0 ? "./" : "../".repeat(depth);
    }
}
