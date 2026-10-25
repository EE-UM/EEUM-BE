package com.eeum.domain.admin.controller;

import com.eeum.global.securitycore.admin.AdminPrincipal;
import com.eeum.global.securitycore.admin.AdminSecurityConfig;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Hidden
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("kakaoLoginUrl",
            "/admin/oauth2/authorization/" + AdminSecurityConfig.REGISTRATION_ID);
        return "admin/login";
    }

    @GetMapping
    public String index(@AuthenticationPrincipal AdminPrincipal admin, Model model) {
        model.addAttribute("admin", admin);
        return "admin/index";
    }
}
