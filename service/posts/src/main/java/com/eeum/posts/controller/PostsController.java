package com.eeum.posts.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    @PostMapping
    public ApiResponse<?> createPost(
//            @CurrentUser
    ) {
        return null;
    }
}
