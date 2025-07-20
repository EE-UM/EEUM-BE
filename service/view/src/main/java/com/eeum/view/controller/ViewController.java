package com.eeum.view.controller;

import com.eeum.common.response.ApiResponse;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import com.eeum.view.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/view")
public class ViewController {

    private final ViewService viewService;

    @PostMapping("/posts/{postId}")
    public ApiResponse<Long> increase(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.success(viewService.increase(postId, userPrincipal.getId()));
    }

    @GetMapping("/posts/{postId}/count")
    public ApiResponse<Long> count(@PathVariable("postId") Long postId) {
        return ApiResponse.success(viewService.count(postId));
    }
}
