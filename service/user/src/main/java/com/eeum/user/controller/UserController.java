package com.eeum.user.controller;

import com.eeum.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping
    public ApiResponse<?> test() {
        System.out.println("hello");
        return ApiResponse.success("test");
    }
}
