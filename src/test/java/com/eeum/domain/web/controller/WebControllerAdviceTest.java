package com.eeum.domain.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class WebControllerAdviceTest {

    @Test
    void 게시글_경로는_한_단계_위를_웹_루트로_쓴다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/p/1");
        request.setServletPath("/p/1");

        assertThat(WebControllerAdvice.baseOf(request)).isEqualTo("../");
    }

    @Test
    void 루트는_현재_경로를_웹_루트로_쓴다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setServletPath("/");

        assertThat(WebControllerAdvice.baseOf(request)).isEqualTo("./");
    }
}
