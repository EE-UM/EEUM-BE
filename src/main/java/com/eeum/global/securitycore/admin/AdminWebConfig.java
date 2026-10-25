package com.eeum.global.securitycore.admin;

import jakarta.servlet.SessionCookieConfig;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * nginx 뒤에서 관리자 페이지가 올바른 주소·쿠키로 동작하도록 하는 서블릿 설정.
 * <ul>
 *   <li>/admin/* 요청에만 X-Forwarded-Proto/Host/Port/Prefix/For 를 반영한다. https 콜백 주소, /dev 접두어가 붙은
 *   링크·리다이렉트, 감사 로그의 실제 접속 IP 가 여기서 결정된다. nginx 가 이 헤더들을 모두 덮어써야 한다
 *   (infra/nginx/eeum.conf 의 admin location).</li>
 *   <li>dev/prod 가 같은 도메인(eeum.site)을 쓰므로 세션 쿠키 이름을 프로파일별로 분리하고, local 외에는 Secure 를 강제한다.</li>
 * </ul>
 */
@Configuration
public class AdminWebConfig {

    private final String sessionCookieName;
    private final boolean secureCookie;

    public AdminWebConfig(Environment environment) {
        this.sessionCookieName = sessionCookieName(environment.getActiveProfiles());
        this.secureCookie = !environment.acceptsProfiles(Profiles.of("local"));
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> adminForwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration =
            new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        registration.addUrlPatterns("/admin/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    public ServletContextInitializer adminSessionCookieInitializer() {
        return servletContext -> {
            SessionCookieConfig cookie = servletContext.getSessionCookieConfig();
            cookie.setName(sessionCookieName);
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
        };
    }

    @Bean
    public CookieSameSiteSupplier adminSessionCookieSameSiteSupplier() {
        // 카카오 → 콜백 리다이렉트(cross-site top-level GET)에 세션 쿠키가 실려야 하므로 Strict 가 아닌 Lax
        return CookieSameSiteSupplier.ofLax().whenHasName(sessionCookieName);
    }

    static String sessionCookieName(String[] activeProfiles) {
        String suffix = Arrays.stream(activeProfiles)
            .map(profile -> profile.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_"))
            .collect(Collectors.joining("_"));
        return suffix.isEmpty() ? "EEUM_ADMIN_SESSION" : "EEUM_ADMIN_SESSION_" + suffix;
    }
}
