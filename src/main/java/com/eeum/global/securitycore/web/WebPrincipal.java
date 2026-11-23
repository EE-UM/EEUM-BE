package com.eeum.global.securitycore.web;

import com.eeum.global.securitycore.token.UserPrincipal;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * 공개 웹(eeum.site) 카카오 로그인 사용자. onboarded 가 false 면 약관 동의·닉네임 입력(/w/signup)을 먼저 마쳐야 한다.
 */
@Getter
public class WebPrincipal extends DefaultOidcUser {

    public static final String ROLE_WEB_USER = "ROLE_WEB_USER";
    private static final List<GrantedAuthority> AUTHORITIES =
        List.of(new SimpleGrantedAuthority(ROLE_WEB_USER));

    private final Long userId;
    private final String nickname;
    private final String username;
    private final String email;
    private final boolean onboarded;

    public WebPrincipal(Long userId, String nickname, String username, String email,
        boolean onboarded, OidcIdToken idToken) {
        super(AUTHORITIES, idToken, "sub");
        this.userId = userId;
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.onboarded = onboarded;
    }

    public WebPrincipal onboardedAs(String nickname) {
        return new WebPrincipal(userId, nickname, username, email, true, getIdToken());
    }

    /**
     * 앱 API 와 같은 서비스(CommentService 등)를 호출하기 위한 변환.
     */
    public UserPrincipal toUserPrincipal() {
        return new UserPrincipal(userId, email, username, "USER", AUTHORITIES);
    }
}
