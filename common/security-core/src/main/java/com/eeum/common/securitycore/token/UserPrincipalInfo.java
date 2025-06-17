package com.eeum.common.securitycore.token;

public interface UserPrincipalInfo {
    Long getId();
    String getEmail();
    String getUsername();
    String getRole();
}
