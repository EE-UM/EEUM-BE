package com.eeum.global.config;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostsRandomShakeWarmUp implements ApplicationRunner {

    private final PostsRepository postsRepository;
    private final PostsRandomShakeRepository postsRandomShakeRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[Warm Up] Redis Random Pool Initialize Start");

        List<Posts> activePosts = postsRepository.findAllActivePosts();

        List<ShowRandomStoryOnShakeResponse> candidates = activePosts.stream()
                .map(p -> new ShowRandomStoryOnShakeResponse(
                        String.valueOf(p.getId()),
                        String.valueOf(p.getUserId()),
                        p.getTitle(),
                        p.getContent()
                ))
                .collect(Collectors.toList());

        postsRandomShakeRepository.resetAndWarm(candidates);

        log.info("[Warm Up] Redis Random Pool Initialize Completed. count={}", candidates.size());
    }
}
