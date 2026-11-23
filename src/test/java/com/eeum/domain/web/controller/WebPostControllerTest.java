package com.eeum.domain.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.eeum.domain.web.service.WebPostService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WebPostControllerTest {

    @Mock
    private WebPostService webPostService;
    @InjectMocks
    private WebPostController webPostController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webPostController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @Test
    void 루트는_상대_경로로_리다이렉트한다() throws Exception {
        given(webPostService.pickRandomPostId()).willReturn(Optional.of(5L));

        // 절대 URL 이 되면 nginx 뒤에서 https·/dev 접두어를 잃는다
        mockMvc.perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "p/5"))
            .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void 사연이_없으면_안내_페이지를_보여준다() throws Exception {
        given(webPostService.pickRandomPostId()).willReturn(Optional.empty());

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("web/message"));
    }
}
