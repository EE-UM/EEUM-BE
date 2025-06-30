package com.eeum.posts.service;

import com.eeum.common.snowflake.Snowflake;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.response.CreatePostResponse;
import com.eeum.posts.dto.response.GetMyPostsResponse;
import com.eeum.posts.dto.response.GetPostByIdResponse;
import com.eeum.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.posts.entity.Album;
import com.eeum.posts.entity.Posts;
import com.eeum.posts.exception.NoAvailablePostsException;
import com.eeum.posts.exception.PostsNotFoundException;
import com.eeum.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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

    public GetPostByIdResponse getPostById(Long id, Long postId) {
        Posts posts = postsRepository.findById(postId)
                .orElseThrow(PostsNotFoundException::new);

        return new GetPostByIdResponse(String.valueOf(posts.getId()), posts.getTitle(), posts.getContent(), posts.getAlbum().getSongName(),
                posts.getAlbum().getArtistName(), posts.getAlbum().getArtworkUrl(), posts.getAlbum().getAppleMusicUrl(),
                posts.getCreatedAt());
    }

    public List<GetMyPostsResponse> getMyPosts(Long userId) {
        // 정렬 조건과 페이징 조건이 기획되면 쿼리문 수정 예정
        List<Posts> posts = postsRepository.findByUserId(userId);
        return posts.stream().map(post -> new GetMyPostsResponse(
                String.valueOf(post.getUserId()),
                post.getTitle(),
                post.getAlbum().getArtworkUrl(),
                post.getIsCompleted()
        )).toList();
    }
}
