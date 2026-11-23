package com.eeum.global.securitycore.web;

import jakarta.servlet.http.HttpSession;
import java.util.regex.Pattern;

/**
 * 로그인·가입을 마친 뒤 돌아갈 웹 경로. 외부 주소로 보내는 open redirect 를 막기 위해 허용된 웹 경로만 저장한다.
 */
public final class WebReturnTo {

    static final String SESSION_KEY = "EEUM_WEB_RETURN_TO";
    private static final Pattern ALLOWED = Pattern.compile("^/(p/\\d{1,19}|w/write)?$");
    private static final String DEFAULT = "/";

    private WebReturnTo() {
    }

    public static boolean isAllowed(String path) {
        return path != null && ALLOWED.matcher(path).matches();
    }

    public static void save(HttpSession session, String path) {
        if (isAllowed(path)) {
            session.setAttribute(SESSION_KEY, path);
        } else {
            session.removeAttribute(SESSION_KEY);
        }
    }

    /**
     * 저장된 경로를 꺼내고 지운다. 없으면 루트.
     */
    public static String consume(HttpSession session) {
        if (session == null) {
            return DEFAULT;
        }
        Object saved = session.getAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY);
        return saved instanceof String path && isAllowed(path) ? path : DEFAULT;
    }
}
