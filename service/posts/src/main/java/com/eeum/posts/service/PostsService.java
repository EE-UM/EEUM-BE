package com.eeum.posts.service;

import com.eeum.common.snowflake.Snowflake;
import com.eeum.posts.dto.request.CreatePostRequest;
import com.eeum.posts.dto.request.UpdatePostRequest;
import com.eeum.posts.dto.response.*;
import com.eeum.posts.entity.Album;
import com.eeum.posts.entity.Posts;
import com.eeum.posts.exception.NoAvailablePostsException;
import com.eeum.posts.exception.PostsNotFoundException;
import com.eeum.posts.repository.PostsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
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

    @Transactional
    public UpdatePostResponse updatePost(Long userId, UpdatePostRequest updatePostRequest) {
        Posts posts = postsRepository.findByIdAndUserId(updatePostRequest.postId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글 정보를 찾을 수 없습니다."));

        posts.update(updatePostRequest.title(), updatePostRequest.content());

        return UpdatePostResponse.from(posts);
    }

    @Transactional
    public Long delete(Long postId) {
        postsRepository.deleteById(postId);
        return postId;
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

    public GetPostByIdResponse getPostById(Long postId) {
        Posts posts = postsRepository.findById(postId)
                .orElseThrow(PostsNotFoundException::new);

        return new GetPostByIdResponse(String.valueOf(posts.getId()), posts.getTitle(), posts.getContent(), String.valueOf(posts.getUserId()), posts.getAlbum().getSongName(),
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

    public List<ReadAllInfiiniteScrollResponse> readAllInfiiniteScroll(Long pageSize, Long lastPostId) {
        List<Posts> posts = lastPostId == null ?
                postsRepository.findAllInfiniteScroll(pageSize) :
                postsRepository.findAllInfiniteScroll(pageSize, lastPostId);
        return posts.stream().map(ReadAllInfiiniteScrollResponse::from).toList();
    }
}
