package com.eeum.global.securitycore.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 가입을 마치지 않았으면 가입 화면으로, 아니면 로그인 전에 보던 웹 경로로 보낸다. 돌아갈 경로는 가입 완료 후에도 쓰도록 가입 전에는 꺼내지
 * 않는다.
 */
public class WebLoginSuccessHandler implements AuthenticationSuccessHandler {

    static final String SIGNUP_PATH = "/w/signup";

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        boolean onboarded = authentication.getPrincipal() instanceof WebPrincipal principal
            && principal.isOnboarded();
        String target = onboarded ? WebReturnTo.consume(request.getSession(false)) : SIGNUP_PATH;
        redirectStrategy.sendRedirect(request, response, target);
    }
}
