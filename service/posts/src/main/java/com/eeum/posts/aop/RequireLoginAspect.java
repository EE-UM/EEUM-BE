package com.eeum.posts.aop;

import com.eeum.common.securitycore.token.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RequireLoginAspect {

    @Before("@annotation(requireLogin)")
    public void checkAuthentication(JoinPoint joinPoint, RequireLogin requireLogin) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal) || userPrincipal == null) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }
    }
}
