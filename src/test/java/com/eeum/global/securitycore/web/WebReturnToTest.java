package com.eeum.global.securitycore.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

class WebReturnToTest {

    @Test
    void 웹_경로만_허용한다() {
        assertThat(WebReturnTo.isAllowed("/")).isTrue();
        assertThat(WebReturnTo.isAllowed("/p/900000000000000204")).isTrue();
        assertThat(WebReturnTo.isAllowed("/w/write")).isTrue();
        assertThat(WebReturnTo.isAllowed("//evil.com")).isFalse();
        assertThat(WebReturnTo.isAllowed("https://evil.com")).isFalse();
        assertThat(WebReturnTo.isAllowed("/p/1/../../admin")).isFalse();
        assertThat(WebReturnTo.isAllowed(null)).isFalse();
    }

    @Test
    void 꺼내면_지워지고_없으면_루트다() {
        MockHttpSession session = new MockHttpSession();
        WebReturnTo.save(session, "/p/3");

        assertThat(WebReturnTo.consume(session)).isEqualTo("/p/3");
        assertThat(WebReturnTo.consume(session)).isEqualTo("/");
        assertThat(WebReturnTo.consume(null)).isEqualTo("/");
    }

    @Test
    void 로그인_진입점은_GET_화면_경로만_next로_넘긴다() {
        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/w/write");
        get.setServletPath("/w/write");
        MockHttpServletRequest post = new MockHttpServletRequest("POST", "/w/p/1/comments");
        post.setServletPath("/w/p/1/comments");

        assertThat(WebSecurityConfig.loginPathFor(get)).isEqualTo("/w/login?next=/w/write");
        assertThat(WebSecurityConfig.loginPathFor(post)).isEqualTo("/w/login");
    }
}
