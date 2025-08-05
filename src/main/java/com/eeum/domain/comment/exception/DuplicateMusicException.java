package com.eeum.domain.comment.exception;

public class DuplicateMusicException extends RuntimeException {
    public DuplicateMusicException(String reason) {
        super(reason);
    }
}
