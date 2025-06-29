package com.eeum.posts.exception;

public class PostsNotFoundException extends RuntimeException {
    public PostsNotFoundException() {
        super("This Post is Not Found.");
    }
}
