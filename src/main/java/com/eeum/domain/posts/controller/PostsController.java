package com.eeum.domain.posts.controller;

import com.eeum.global.support.response.ApiResponse;
import com.eeum.global.securitycore.token.CurrentUser;
import com.eeum.global.securitycore.token.UserPrincipal;
import com.eeum.domain.posts.dto.response.*;
import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.dto.request.UpdatePostRequest;
import com.eeum.domain.posts.service.PostsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    @PostMapping
    public ApiResponse<CreatePostResponse> createPost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody @Valid CreatePostRequest createPostRequest
    ) {
        return ApiResponse.success(postsService.createPost(userPrincipal.getId(), createPostRequest));
    }

    @PatchMapping
    public ApiResponse<UpdatePostResponse> updatePost(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody UpdatePostRequest updatePostRequest
    ) {
        return ApiResponse.success(postsService.updatePost(userPrincipal.getId(), updatePostRequest));
    }

    @GetMapping("/ing/infinite-scroll")
    public ApiResponse<List<PostsReadInfiniteScrollResponse>> readAllInfiniteScrollIng(
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId
    ) {
        return ApiResponse.success(postsService.readAllInfiniteScrollIng(pageSize, lastPostId));
    }

    @GetMapping("/done/infinite-scroll")
    public ApiResponse<List<PostsReadInfiniteScrollResponse>> readAllInfiniteScrollDone(
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId
    ) {
        return ApiResponse.success(postsService.readAllInfiniteScrollDone(pageSize, lastPostId));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(String.valueOf(postsService.delete(postId)));
    }

    @GetMapping("/random")
    public ApiResponse<ShowRandomStoryOnShakeResponse> showRandomStoryOnShake(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.showRandomStoryOnShake(userPrincipal.getId()));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostsReadResponse> getPostById(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postsService.read(userPrincipal.getId(), postId));
    }

    @GetMapping("/my")
    public ApiResponse<List<GetMyPostsResponse>> getMyPosts(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.getMyPosts(userPrincipal.getId()));
    }

    @PatchMapping("/{postId}/complete")
    public ApiResponse<CompletePostResponse> completePost(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(postsService.completePost(userPrincipal.getId(), postId));
    }

    @GetMapping("/liked")
    public ApiResponse<List<GetLikedPostsResponse>> getLikedPosts(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.getLikedPosts(userPrincipal.getId()));
    }

    @GetMapping("/commented")
    public ApiResponse<List<GetCommentedPostsResponse>> getCommentedPosts(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(postsService.getCommentedPosts(userPrincipal.getId()));
    }
}
