package com.eeum.domain.posts.exception;

public class PostsNotFoundException extends RuntimeException {
    public PostsNotFoundException() {
        super("This Post is Not Found.");
    }
}
