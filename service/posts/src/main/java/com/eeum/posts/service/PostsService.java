package com.eeum.posts.service;

import com.eeum.common.response.ApiResponse;
import com.eeum.common.snowflake.Snowflake;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.response.CreatePostResponse;
import com.eeum.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.posts.entity.Album;
import com.eeum.posts.entity.Posts;
import com.eeum.posts.exception.NoAvailablePostsException;
import com.eeum.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostsService {

    private final Snowflake snowflake = new Snowflake();
    private final PostsRepository postsRepository;

    @Transactional
    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        Album album = Album.of(createPostRequest.albumName(), createPostRequest.songName(), createPostRequest.artistName(), createPostRequest.artworkUrl(), createPostRequest.appleMusicUrl());
        Posts posts = Posts.of(snowflake.nextId(), createPostRequest.title(), createPostRequest.content(), album, userId);
        postsRepository.save(posts);
        return CreatePostResponse.of(posts.getId(), userId);
    }

    public ShowRandomStoryOnShakeResponse showRandomStoryOnShake(Long userId) {
        Random random = new Random();

        List<Long> postIds = postsRepository.findAllIdsIsNotCompletedPosts(userId);

        if (postIds.isEmpty()) {
            throw new NoAvailablePostsException();
        }

        Long pickedPostId = postIds.get(random.nextInt(postIds.size()));

        Posts posts = postsRepository.findById(pickedPostId)
                .orElseThrow(() -> new NullPointerException("Posts repository is empty."));

//        Posts randomPost = postsRepository.findRandomPost()
//                .orElseThrow(() -> new NullPointerException("Posts repository is empty."));

        return new ShowRandomStoryOnShakeResponse(String.valueOf(posts.getId()), String.valueOf(posts.getUserId()), posts.getTitle(), posts.getContent());
    }

    public Object getPostById(Long id, String postId) {
        return null;
    }
}
