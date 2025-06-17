package com.eeum.posts.service;

import com.eeum.common.response.ApiResponse;
import com.eeum.common.snowflake.Snowflake;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.response.CreatePostResponse;
import com.eeum.posts.entity.Posts;
import com.eeum.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostsService {

    private final Snowflake snowflake = new Snowflake();
    private final PostsRepository postsRepository;

    @Transactional
    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        Posts posts = Posts.of(snowflake.nextId(), createPostRequest.title(), createPostRequest.content(), userId);
        postsRepository.save(posts);
        return CreatePostResponse.of(posts.getId(), userId);
    }
}
