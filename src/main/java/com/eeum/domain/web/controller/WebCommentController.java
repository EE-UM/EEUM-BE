package com.eeum.domain.web.controller;

import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import com.eeum.domain.comment.exception.DuplicateMusicException;
import com.eeum.domain.comment.service.CommentService;
import com.eeum.domain.web.dto.WebCommentForm;
import com.eeum.global.securitycore.web.WebPrincipal;
import com.eeum.global.securitycore.web.WebReturnTo;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 공개 웹 노래 댓글 작성. 작성 후 게시글로 돌아간다(PRG). 본인 사연에도 남길 수 있다(알림은 CommentService 가 본인 댓글이면 보내지
 * 않는다).
 */
@Slf4j
@Hidden
@Controller
@RequiredArgsConstructor
public class WebCommentController {

    private final CommentService commentService;

    @PostMapping("/w/p/{postId}/comments")
    public String create(@PathVariable("postId") Long postId,
        @AuthenticationPrincipal WebPrincipal viewer,
        @Valid @ModelAttribute("commentForm") WebCommentForm form, BindingResult bindingResult,
        HttpSession session) {
        if (!viewer.isOnboarded()) {
            WebReturnTo.save(session, "/p/" + postId);
            return "redirect:/w/signup";
        }
        if (bindingResult.hasErrors()) {
            return redirectToPost(postId,
                bindingResult.hasFieldErrors("content") ? "too-long" : "music-required");
        }

        try {
            commentService.create(viewer.toUserPrincipal(), form.toRequest(postId));
        } catch (AlreadyFinishedPostException e) {
            return redirectToPost(postId, "finished");
        } catch (DuplicateMusicException e) {
            return redirectToPost(postId, "duplicate");
        } catch (IllegalArgumentException e) {
            log.warn("[WebComment] 댓글 작성 실패 - postId={}, reason={}", postId, e.getMessage());
            return redirectToPost(postId, "failed");
        }
        return redirectToPost(postId, "commented");
    }

    private static String redirectToPost(Long postId, String notice) {
        return "redirect:/p/" + postId + "?notice=" + notice;
    }
}
