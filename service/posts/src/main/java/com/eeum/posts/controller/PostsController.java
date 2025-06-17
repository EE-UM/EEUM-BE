package com.eeum.posts.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.posts.aop.RequireLogin;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.response.CreatePostResponse;
import com.eeum.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    @RequireLogin
    @PostMapping
    public ApiResponse<CreatePostResponse> createPost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody CreatePostRequest createPostRequest
            ) {
        System.out.println("userPrincipal = " + userPrincipal);
        System.out.println("userPrincipal.getEmail() = " + userPrincipal.getEmail());
        return ApiResponse.success(postsService.createPost(userPrincipal.getId(), createPostRequest));
    }
}
