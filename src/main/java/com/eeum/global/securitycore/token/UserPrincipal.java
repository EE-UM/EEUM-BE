package com.eeum.global.securitycore.token;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final String email;
    private final String username;
    private final String role;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String username, String role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.authorities = authorities;
    }

//    public static UserPrincipal createUser(User user) {
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
//        return new UserPrincipal(
//                user.getId(),
//                user.getEmail(),
//                user.getUsername(),
//                user.getRole(),
//                authorities
//        );
//    }

    public static UserPrincipal from(UserPrincipalInfo user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                authorities
        );
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
