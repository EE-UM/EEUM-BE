package com.eeum.global.securitycore.admin;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

@Getter
public class AdminPrincipal extends DefaultOidcUser {

    private final Long userId;
    private final String nickname;

    public AdminPrincipal(Long userId, String nickname,
        Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken) {
        super(authorities, idToken, "sub");
        this.userId = userId;
        this.nickname = nickname;
    }
}
