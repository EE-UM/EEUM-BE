package com.eeum.domain.admin.controller;

import com.eeum.domain.admin.dto.request.AdminPostSearchCondition;
import com.eeum.domain.admin.dto.request.AdminPostStatus;
import com.eeum.domain.admin.exception.InvalidAdminActionException;
import com.eeum.domain.admin.service.AdminPostService;
import com.eeum.global.securitycore.admin.AdminPrincipal;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Hidden
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    public String list(@AuthenticationPrincipal AdminPrincipal admin,
        @RequestParam(required = false) AdminPostStatus status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "0") int page,
        Model model) {
        AdminPostSearchCondition condition = new AdminPostSearchCondition(status, keyword, userId,
            page);
        model.addAttribute("admin", admin);
        model.addAttribute("condition", condition);
        model.addAttribute("statuses", AdminPostStatus.values());
        model.addAttribute("posts", adminPostService.search(condition));
        return "admin/posts/list";
    }

    @GetMapping("/{postId}")
    public String detail(@AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long postId, Model model) {
        model.addAttribute("admin", admin);
        model.addAttribute("detail", adminPostService.getDetail(postId));
        return "admin/posts/detail";
    }

    @PostMapping("/{postId}/delete")
    public String delete(@AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long postId,
        @RequestParam(required = false) String reason,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes) {
        try {
            adminPostService.delete(postId, admin.getUserId(), reason, request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("message", "게시글을 삭제했습니다.");
        } catch (InvalidAdminActionException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/posts/" + postId;
    }

    @PostMapping("/{postId}/restore")
    public String restore(@AuthenticationPrincipal AdminPrincipal admin,
        @PathVariable Long postId,
        @RequestParam(required = false) String reason,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes) {
        try {
            adminPostService.restore(postId, admin.getUserId(), reason, request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("message", "게시글을 복구했습니다.");
        } catch (InvalidAdminActionException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/posts/" + postId;
    }
}
