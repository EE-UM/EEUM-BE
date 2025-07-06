package com.eeum.postsread.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.postsread.dto.response.PostsReadResponse;
import com.eeum.postsread.service.PostsReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts-read")
public class PostsReadController {
    private final PostsReadService postsReadService;

    @GetMapping("/{postId}")
    public ApiResponse<PostsReadResponse> read(@PathVariable("postId") Long postId) {
        PostsReadResponse response = postsReadService.read(postId);
        return ApiResponse.success(response);
    }
}
