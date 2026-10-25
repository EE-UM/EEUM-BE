package com.eeum.domain.admin.controller;

import com.eeum.domain.admin.exception.AdminPostNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 관리자 컨트롤러의 예외를 JSON(ApiControllerAdvice) 대신 관리자 에러 페이지로 응답한다.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackageClasses = AdminController.class)
public class AdminControllerAdvice {

    @ExceptionHandler(AdminPostNotFoundException.class)
    public ModelAndView handleNotFound(AdminPostNotFoundException e) {
        return errorPage(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class})
    public ModelAndView handleBadRequest(Exception e) {
        return errorPage(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception e) {
        log.error("[Admin] 처리되지 않은 예외", e);
        return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했습니다. 로그를 확인해 주세요.");
    }

    private ModelAndView errorPage(HttpStatus status, String message) {
        ModelAndView modelAndView = new ModelAndView("admin/error");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
