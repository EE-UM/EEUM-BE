package com.eeum.global.aop.auth;

import com.eeum.global.securitycore.token.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class RequireLoginAspect {
    @Before("@annotation(requireLogin)")
    public void checkAuthentication(JoinPoint joinPoint, RequireLogin requireLogin) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal) || userPrincipal == null) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }
    }
}
