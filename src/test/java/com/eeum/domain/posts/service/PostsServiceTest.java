package com.eeum.domain.posts.service;

import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.like.repository.LikeRepository;
import com.eeum.domain.notification.publisher.SpamFilterPublisher;
import com.eeum.domain.posts.dto.request.CreatePostRequest;
import com.eeum.domain.posts.dto.response.CompletePostResponse;
import com.eeum.domain.posts.dto.response.CreatePostResponse;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.entity.PostsCommentCount;
import com.eeum.domain.posts.exception.NoAvailablePostsException;
import com.eeum.domain.posts.repository.PostsCommentCountRepository;
import com.eeum.domain.posts.repository.PostsIdListRepository;
import com.eeum.domain.posts.repository.PostsQueryModelRepository;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.view.service.ViewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {

    @InjectMocks
    private PostsService postsService;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private PostsQueryModelRepository postsQueryModelRepository;

    @Mock
    private PostsIdListRepository postsIdListRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentCountRepository commentCountRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ViewService viewService;

    @Mock
    private PostsRandomShakeRepository postsRandomShakeRepository;

    @Mock
    private PostsCommentCountRepository postsCommentCountRepository;

    @Mock
    private SpamFilterPublisher spamFilterPublisher;

    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 100L;

    private Posts createPost(Long userId, boolean isCompleted) {
        Album album = Album.of("앨범", "노래", "아티스트", "http://art.url", "http://music.url");
        Posts post = Posts.of("제목", "내용", album, userId);
        post.updateCompletionType(CompletionType.MANUAL_COMPLETION);
        if (isCompleted) {
            post.updateIsCompleted();
        }
        return post;
    }

    @Test
    @DisplayName("AUTO_COMPLETION 타입에 commentCountLimit 이 null 이면 createPost 시 예외 발생")
    void createPost_autoCompletionWithoutLimit_throws() {
        CreatePostRequest request = new CreatePostRequest(
                "제목", "내용", "앨범", "노래", "아티스트", "url", "url2",
                CompletionType.AUTO_COMPLETION, null // limit 없음
        );

        assertThatThrownBy(() -> postsService.createPost(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment count limit");

        verify(postsRepository, never()).save(any());
    }

    @Test
    @DisplayName("MANUAL_COMPLETION 타입으로 createPost 호출 시 게시물 저장 및 응답 반환")
    void createPost_manualCompletion_success() {
        CreatePostRequest request = new CreatePostRequest(
                "제목", "내용", "앨범", "노래", "아티스트", "url", "url2",
                CompletionType.MANUAL_COMPLETION, null
        );
        Posts savedPost = createPost(USER_ID, false);
        given(postsRepository.save(any(Posts.class))).willReturn(savedPost);

        CreatePostResponse response = postsService.createPost(USER_ID, request);

        assertThat(response).isNotNull();
        verify(postsRepository).save(any(Posts.class));
        verify(commentCountRepository).save(any());
        verify(postsRandomShakeRepository).addCandidate(any());
        verify(spamFilterPublisher).publish(any());
        verify(postsCommentCountRepository).save(any());
    }

    @Test
    @DisplayName("AUTO_COMPLETION 타입에 commentCountLimit 이 있으면 createPost 성공")
    void createPost_autoCompletion_withLimit_success() {
        CreatePostRequest request = new CreatePostRequest(
                "제목", "내용", "앨범", "노래", "아티스트", "url", "url2",
                CompletionType.AUTO_COMPLETION, 5L
        );
        Posts savedPost = createPost(USER_ID, false);
        given(postsRepository.save(any(Posts.class))).willReturn(savedPost);

        CreatePostResponse response = postsService.createPost(USER_ID, request);

        assertThat(response).isNotNull();
        verify(postsRepository).save(any(Posts.class));
    }

    @Test
    @DisplayName("completePost 호출 시 해당 게시물의 isCompleted 가 true 로 변경됨")
    void completePost_setsIsCompleted() {
        Posts post = createPost(USER_ID, false);
        given(postsRepository.findByIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.of(post));

        CompletePostResponse response = postsService.completePost(USER_ID, POST_ID);

        assertThat(response).isNotNull();
        assertThat(response.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("completePost 호출 시 게시물을 찾을 수 없으면 예외 발생")
    void completePost_throwsWhenPostNotFound() {
        given(postsRepository.findByIdAndUserId(POST_ID, USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postsService.completePost(USER_ID, POST_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("showRandomStoryOnShake 호출 시 Redis 에 게시물이 없으면 NoAvailablePostsException 발생")
    void showRandomStoryOnShake_throwsWhenNoPost() {
        given(postsRandomShakeRepository.pickRandom()).willReturn(Optional.empty());

        assertThatThrownBy(() -> postsService.showRandomStoryOnShake())
                .isInstanceOf(NoAvailablePostsException.class);
    }

    @Test
    @DisplayName("delete 호출 시 게시물이 없으면 IllegalArgumentException 발생")
    void delete_throwsWhenPostNotFound() {
        given(postsRepository.findById(POST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postsService.delete(USER_ID, POST_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
