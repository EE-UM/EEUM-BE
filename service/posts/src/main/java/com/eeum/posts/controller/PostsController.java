package com.eeum.posts.controller;

import com.eeum.common.aop.auth.RequireLogin;
import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.response.CreatePostResponse;
import com.eeum.posts.dto.response.GetMyPostsResponse;
import com.eeum.posts.dto.response.GetPostByIdResponse;
import com.eeum.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ApiResponse.success(postsService.createPost(userPrincipal.getId(), createPostRequest));
    }

    @RequireLogin
    @GetMapping("/random")
    public ApiResponse<ShowRandomStoryOnShakeResponse> showRandomStoryOnShake(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.showRandomStoryOnShake(userPrincipal.getId()));
    }

    @GetMapping("/{postId}")
    public ApiResponse<GetPostByIdResponse> getPostById(
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postsService.getPostById(postId));
    }

    @RequireLogin
    @GetMapping("/my")
    public ApiResponse<List<GetMyPostsResponse>> getMyPosts(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.getMyPosts(userPrincipal.getId()));
    }
}
