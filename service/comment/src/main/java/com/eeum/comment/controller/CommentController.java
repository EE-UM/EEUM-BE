package com.eeum.comment.controller;

import com.eeum.comment.dto.response.CommentResponse;
import com.eeum.comment.service.CommentService;
import com.eeum.common.aop.auth.RequireLogin;
import com.eeum.common.securitycore.token.CurrentUser;
import com.eeum.common.securitycore.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @RequireLogin
    @GetMapping("/{commentId}")
    public CommentResponse read(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable("commentId") Long commentId
            ) {
        return commentService.read(commentId);
    }
}
