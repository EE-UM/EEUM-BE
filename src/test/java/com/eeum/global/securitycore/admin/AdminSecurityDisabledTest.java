package com.eeum.global.securitycore.admin;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eeum.domain.admin.controller.AdminController;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.SecurityConfig;
import com.eeum.global.securitycore.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringJUnitWebConfig(classes = {
    AdminSecurityConfigTest.TestConfig.class,
    AdminSecurityConfig.class,
    AdminOidcUserService.class,
    SecurityConfig.class,
    AdminController.class
})
class AdminSecurityDisabledTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private WebApplicationContext context;

    @Test
    @DisplayName("admin.kakao.client-id 가 없으면 로그인 페이지를 포함한 /admin/** 전체가 거부된다")
    void withoutKakaoClient_deniesAllAdminPaths() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())
            .build();

        mockMvc.perform(get("/admin/login")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/oauth2/authorization/kakao-admin"))
            .andExpect(status().isForbidden());
    }
}
