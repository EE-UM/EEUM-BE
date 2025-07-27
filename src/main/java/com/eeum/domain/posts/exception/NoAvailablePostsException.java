package com.eeum.domain.posts.exception;

public class NoAvailablePostsException extends RuntimeException {
    public NoAvailablePostsException() {
        super("No available posts for random selection.");
    }
}
