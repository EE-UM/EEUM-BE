package com.eeum.postsread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.eeum.postsread",
        "com.eeum.common.securitycore"
}
)//
public class PostsReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostsReadApplication.class, args);
    }

}
