package com.eeum.postsread.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.postsread.dto.response.PostsReadInfiniteScrollResponse;
import com.eeum.postsread.dto.response.PostsReadResponse;
import com.eeum.postsread.service.PostsReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/infinite-scroll")
    public List<PostsReadInfiniteScrollResponse> readAllInfiniteScroll(
            @RequestParam(value = "lastPostId", required = false) Long lastPostId,
            @RequestParam("pageSize") Long pageSize
    ) {
        return postsReadService.readAllInfiniteScroll(lastPostId, pageSize);
    }
}
