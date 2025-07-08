package com.eeum.posts.controller;

import com.eeum.common.aop.auth.RequireLogin;
import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.request.UpdatePostRequest;
import com.eeum.posts.dto.response.*;
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
    @PatchMapping
    public ApiResponse<UpdatePostResponse> updatePost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody UpdatePostRequest updatePostRequest
            ) {
        return ApiResponse.success(postsService.updatePost(userPrincipal.getId(), updatePostRequest));
    }

    @GetMapping("/infinite-scroll")
    public ApiResponse<List<ReadAllInfiiniteScrollResponse>> readAllInfiniteScroll(
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId
    ) {
        return ApiResponse.success(postsService.readAllInfiiniteScroll(pageSize, lastPostId));
    }

    @RequireLogin
    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(String.valueOf(postsService.delete(postId)));
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
