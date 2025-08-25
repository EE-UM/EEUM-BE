package com.eeum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class EEUMApplication {

    public static void main(String[] args) {
        SpringApplication.run(EEUMApplication.class, args);
    }
}
