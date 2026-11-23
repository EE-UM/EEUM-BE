package com.eeum.domain.web.controller;

import com.eeum.domain.web.dto.WebSignupForm;
import com.eeum.domain.web.service.WebUserService;
import com.eeum.global.securitycore.web.WebPrincipal;
import com.eeum.global.securitycore.web.WebReturnTo;
import com.eeum.global.securitycore.web.WebSecurityConfig;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 공개 웹 카카오 로그인 진입과 가입(약관 동의 + 닉네임).
 */
@Hidden
@Controller
@RequiredArgsConstructor
public class WebAuthController {

    static final String SIGNUP_PATH = "/w/signup";
    static final String TERMS_URL = "https://app.notion.com/p/EEUM-2a8a8ad06b4180cda342f1917395f176";
    static final String PRIVACY_URL = "https://app.notion.com/p/EEUM-2a8a8ad06b4180c293caff349a054c1e";

    private final WebUserService webUserService;

    /**
     * next(로그인 후 돌아갈 웹 경로)를 세션에 담고 카카오 로그인으로 보낸다. 이미 로그인했으면 바로 보낸다.
     */
    @GetMapping("/w/login")
    public String login(@RequestParam(value = "next", required = false) String next,
        @AuthenticationPrincipal WebPrincipal viewer, HttpSession session) {
        WebReturnTo.save(session, next);
        if (viewer == null) {
            return "redirect:/w/oauth2/authorization/" + WebSecurityConfig.REGISTRATION_ID;
        }
        return viewer.isOnboarded() ? "redirect:" + WebReturnTo.consume(session)
            : "redirect:" + SIGNUP_PATH;
    }

    @GetMapping("/w/login/error")
    public String loginError(Model model) {
        model.addAttribute("base", WebPaths.baseOf("/w/login/error"));
        model.addAttribute("heading", "로그인하지 못했어요");
        model.addAttribute("message", "카카오 로그인을 다시 시도해 주세요. 이용이 제한된 계정은 로그인할 수 없어요.");
        return "web/message";
    }

    @GetMapping(SIGNUP_PATH)
    public String signupForm(@AuthenticationPrincipal WebPrincipal viewer, HttpSession session,
        Model model) {
        if (viewer.isOnboarded()) {
            return "redirect:" + WebReturnTo.consume(session);
        }
        return renderSignup(model, WebSignupForm.empty(), null);
    }

    @PostMapping(SIGNUP_PATH)
    public String signup(@AuthenticationPrincipal WebPrincipal viewer,
        @Valid @ModelAttribute("form") WebSignupForm form, BindingResult bindingResult,
        HttpServletRequest request, HttpServletResponse response, Model model) {
        if (viewer.isOnboarded()) {
            return "redirect:" + WebReturnTo.consume(request.getSession(false));
        }
        if (bindingResult.hasErrors()) {
            return renderSignup(model, form, WebWriteController.firstError(bindingResult));
        }

        String nickname = form.nickname().strip();
        webUserService.completeSignup(viewer.getUserId(), nickname);
        refreshPrincipal(viewer.onboardedAs(nickname), request, response);
        return "redirect:" + WebReturnTo.consume(request.getSession(false));
    }

    /**
     * 세션에 저장된 로그인 정보를 가입 완료 상태로 바꾼다 (다시 로그인하지 않아도 쓰기 기능을 쓸 수 있게).
     */
    private static void refreshPrincipal(WebPrincipal principal, HttpServletRequest request,
        HttpServletResponse response) {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        String registrationId = current instanceof OAuth2AuthenticationToken token
            ? token.getAuthorizedClientRegistrationId() : WebSecurityConfig.REGISTRATION_ID;
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new OAuth2AuthenticationToken(principal, principal.getAuthorities(), registrationId));
        SecurityContextHolder.setContext(context);
        WebSecurityConfig.securityContextRepository().saveContext(context, request, response);
    }

    private String renderSignup(Model model, WebSignupForm form, String error) {
        model.addAttribute("base", WebPaths.baseOf(SIGNUP_PATH));
        model.addAttribute("form", form);
        model.addAttribute("error", error);
        model.addAttribute("termsUrl", TERMS_URL);
        model.addAttribute("privacyUrl", PRIVACY_URL);
        model.addAttribute("nicknameMaxLength", WebSignupForm.NICKNAME_MAX_LENGTH);
        return "web/signup";
    }
}
