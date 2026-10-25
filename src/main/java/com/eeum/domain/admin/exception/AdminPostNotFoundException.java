package com.eeum.domain.admin.exception;

public class AdminPostNotFoundException extends RuntimeException {

    public AdminPostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다. (id=" + postId + ")");
    }
}
