package com.eeum.domain.web.controller;

import com.eeum.domain.web.dto.WebWriteForm;
import com.eeum.domain.web.service.WebPostService;
import com.eeum.global.securitycore.web.WebPrincipal;
import com.eeum.global.securitycore.web.WebReturnTo;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 공개 웹 사연 작성 (앱 ShareScreen 과 같은 범위). 스팸 필터·랜덤 풀 등록은 PostsService.createPost 가 처리한다.
 */
@Hidden
@Controller
@RequiredArgsConstructor
public class WebWriteController {

    static final String WRITE_PATH = "/w/write";

    private final WebPostService webPostService;

    @GetMapping(WRITE_PATH)
    public String form(@AuthenticationPrincipal WebPrincipal viewer, HttpSession session,
        Model model) {
        if (!viewer.isOnboarded()) {
            WebReturnTo.save(session, WRITE_PATH);
            return "redirect:/w/signup";
        }
        return render(model, viewer, WebWriteForm.empty(), null);
    }

    @PostMapping(WRITE_PATH)
    public String write(@AuthenticationPrincipal WebPrincipal viewer, HttpSession session,
        @Valid @ModelAttribute("form") WebWriteForm form, BindingResult bindingResult,
        Model model) {
        if (!viewer.isOnboarded()) {
            WebReturnTo.save(session, WRITE_PATH);
            return "redirect:/w/signup";
        }
        if (bindingResult.hasErrors()) {
            return render(model, viewer, form, firstError(bindingResult));
        }
        Long postId = webPostService.write(viewer.getUserId(), form.toRequest());
        return "redirect:/p/" + postId + "?notice=posted";
    }

    private String render(Model model, WebPrincipal viewer, WebWriteForm form, String error) {
        model.addAttribute("base", WebPaths.baseOf(WRITE_PATH));
        model.addAttribute("viewer", viewer);
        model.addAttribute("form", form);
        model.addAttribute("error", error);
        model.addAttribute("limitOptions", WebWriteForm.LIMIT_OPTIONS);
        model.addAttribute("titleMaxLength", WebWriteForm.TITLE_MAX_LENGTH);
        model.addAttribute("contentMaxLength", WebWriteForm.CONTENT_MAX_LENGTH);
        return "web/write";
    }

    static String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .findFirst()
            .orElse("입력값을 확인해 주세요.");
    }
}
