package com.eeum.domain.comment.exception;

public class AlreadyFinishedPostException extends RuntimeException {
    public AlreadyFinishedPostException(String reason) {
        super(reason);
    }
}
