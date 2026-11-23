package com.eeum.domain.web.controller;

import com.eeum.domain.posts.exception.PostsNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 공개 웹 페이지의 예외를 JSON(ApiControllerAdvice) 대신 안내 페이지로 응답한다.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackageClasses = WebPostController.class)
public class WebControllerAdvice {

    @ExceptionHandler({PostsNotFoundException.class, MethodArgumentTypeMismatchException.class})
    public ModelAndView handleNotFound(HttpServletRequest request) {
        return messagePage(request, HttpStatus.NOT_FOUND, "사연을 찾을 수 없어요",
            "삭제되었거나 존재하지 않는 사연이에요.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("[Web] 처리되지 않은 예외 - uri: {}", request.getRequestURI(), e);
        return messagePage(request, HttpStatus.INTERNAL_SERVER_ERROR, "잠시 문제가 생겼어요",
            "잠시 후 다시 시도해 주세요.");
    }

    private ModelAndView messagePage(HttpServletRequest request, HttpStatus status,
        String heading, String message) {
        ModelAndView modelAndView = new ModelAndView("web/message");
        modelAndView.setStatus(status);
        modelAndView.addObject("base", baseOf(request));
        modelAndView.addObject("heading", heading);
        modelAndView.addObject("message", message);
        return modelAndView;
    }

    static String baseOf(HttpServletRequest request) {
        return request.getServletPath().startsWith("/p/")
            ? WebPostController.POST_BASE
            : WebPostController.ROOT_BASE;
    }
}
