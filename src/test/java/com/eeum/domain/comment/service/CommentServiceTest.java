package com.eeum.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.comment.dto.request.CommentCreateRequest;
import com.eeum.domain.comment.dto.response.CommentResponse;
import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.entity.CommentCount;
import com.eeum.domain.comment.event.CommentLimitReachedEvent;
import com.eeum.domain.comment.event.FirstCommentCreatedEvent;
import com.eeum.domain.comment.event.HalfCommentReachedEvent;
import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import com.eeum.domain.comment.exception.DuplicateMusicException;
import com.eeum.domain.comment.producer.CommentProducer;
import com.eeum.domain.comment.repository.CommentCountRepository;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.user.entity.User;
import com.eeum.domain.user.repository.UserRepository;
import com.eeum.global.securitycore.token.UserPrincipal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentCountRepository commentCountRepository;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentProducer commentProducer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long OTHER_USER_ID = 20L;

    private UserPrincipal createUserPrincipal() {
        return new UserPrincipal(USER_ID, "test@test.com", "testUser", "ROLE_USER",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private Posts createPost(String albumName, String artistName, boolean isCompleted) {
        Album album = Album.of(albumName, "노래", artistName, "http://art.url", "http://music.url");
        Posts post = Posts.of("제목", "내용", album, USER_ID);
        post.updateCompletionType(CompletionType.MANUAL_COMPLETION);
        if (isCompleted) {
            post.updateIsCompleted();
        }
        return post;
    }

    @Test
    @DisplayName("정상 요청 시 댓글 생성 성공 및 CommentResponse 반환")
    void create_success() {
        UserPrincipal userPrincipal = createUserPrincipal();
        CommentCreateRequest request = new CommentCreateRequest(
            "댓글 내용", "다른앨범", "다른노래", "다른아티스트", "url", "url2", POST_ID);

        CommentCount commentCount = CommentCount.of(POST_ID, 1L, 5L);
        Posts post = createPost("포스트앨범", "포스트아티스트", false);

        User user = User.of("닉네임", "testUser", "test@test.com", "ROLE_USER", "provider",
            "providerId", true);

        given(commentCountRepository.findByPostId(POST_ID)).willReturn(Optional.of(commentCount));
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

        CommentResponse response = commentService.create(userPrincipal, request);

        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("댓글 내용");
        assertThat(response.nickname()).isEqualTo("닉네임");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("CommentCount 가 없을 때 댓글 생성 시 IllegalArgumentException 발생")
    void create_throwsWhenCommentCountNotFound() {
        UserPrincipal userPrincipal = createUserPrincipal();
        CommentCreateRequest request = new CommentCreateRequest(
            "내용", "앨범", "노래", "아티스트", "url", "url2", POST_ID);

        given(commentCountRepository.findByPostId(POST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(userPrincipal, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("댓글 수가 한도에 도달했을 때 댓글 생성 시 AlreadyFinishedPostException 발생")
    void create_throwsWhenCommentLimitReached() {
        UserPrincipal userPrincipal = createUserPrincipal();
        CommentCreateRequest request = new CommentCreateRequest(
            "내용", "앨범", "노래", "아티스트", "url", "url2", POST_ID);

        CommentCount commentCount = CommentCount.of(POST_ID, 5L, 5L); // 한도 도달
        Posts post = createPost("포스트앨범", "포스트아티스트", false);

        given(commentCountRepository.findByPostId(POST_ID)).willReturn(Optional.of(commentCount));
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.create(userPrincipal, request))
            .isInstanceOf(AlreadyFinishedPostException.class)
            .hasMessageContaining("comment_limit_reached");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 완료된 게시물에 댓글 생성 시 AlreadyFinishedPostException 발생")
    void create_throwsWhenPostAlreadyCompleted() {
        UserPrincipal userPrincipal = createUserPrincipal();
        CommentCreateRequest request = new CommentCreateRequest(
            "내용", "앨범", "노래", "아티스트", "url", "url2", POST_ID);

        CommentCount commentCount = CommentCount.of(POST_ID, 2L, 5L);
        Posts completedPost = createPost("포스트앨범", "포스트아티스트", true); // isCompleted=true

        given(commentCountRepository.findByPostId(POST_ID)).willReturn(Optional.of(commentCount));
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(completedPost));

        assertThatThrownBy(() -> commentService.create(userPrincipal, request))
            .isInstanceOf(AlreadyFinishedPostException.class)
            .hasMessageContaining("post_completed");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시물과 동일한 음악으로 댓글 생성 시 DuplicateMusicException 발생")
    void create_throwsWhenDuplicateMusic() {
        UserPrincipal userPrincipal = createUserPrincipal();
        // 게시물과 같은 albumName + artistName
        CommentCreateRequest request = new CommentCreateRequest(
            "내용", "같은앨범", "노래", "같은아티스트", "url", "url2", POST_ID);

        CommentCount commentCount = CommentCount.of(POST_ID, 1L, 5L);
        Posts post = createPost("같은앨범", "같은아티스트", false);

        given(commentCountRepository.findByPostId(POST_ID)).willReturn(Optional.of(commentCount));
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.create(userPrincipal, request))
            .isInstanceOf(DuplicateMusicException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시물의 모든 댓글 조회 시 CommentResponse 목록 반환")
    void readAllCommentsOfPost_returnsResponses() {
        com.eeum.domain.comment.entity.Album commentAlbum =
            com.eeum.domain.comment.entity.Album.of("앨범", "노래", "아티스트", "url", "url2");
        Comment comment1 = Comment.of("댓글1", POST_ID, USER_ID, "user1", commentAlbum);
        Comment comment2 = Comment.of("댓글2", POST_ID, USER_ID, "user1", commentAlbum);
        given(commentRepository.findAllByPostsId(POST_ID, USER_ID)).willReturn(
            List.of(comment1, comment2));

        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(USER_ID);
        given(user.getNickname()).willReturn("닉네임");
        given(userRepository.findAllById(List.of(USER_ID))).willReturn(List.of(user));

        List<CommentResponse> result = commentService.readAllCommentsOfPost(POST_ID, USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("댓글1");
        assertThat(result.get(0).nickname()).isEqualTo("닉네임");
        assertThat(result.get(1).content()).isEqualTo("댓글2");
    }

    @Test
    @DisplayName("댓글 삭제 시 softDelete 수행 및 카운트 감소")
    void delete_softDeletesAndDecreases() {
        Long commentId = 99L;
        com.eeum.domain.comment.entity.Album album =
            com.eeum.domain.comment.entity.Album.of("앨범", "노래", "아티스트", "url", "url2");
        Comment comment = Comment.of("내용", POST_ID, USER_ID, "user", album);
        CommentCount commentCount = CommentCount.of(POST_ID, 3L, 5L);

        given(commentRepository.findByIdAndUserId(USER_ID, commentId)).willReturn(
            Optional.of(comment));
        given(commentCountRepository.findById(POST_ID)).willReturn(Optional.of(commentCount));

        commentService.delete(USER_ID, commentId);

        verify(commentRepository).softDelete(commentId);
        assertThat(commentCount.getCommentCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글이 없을 때 delete 호출 시 아무 작업도 하지 않음")
    void delete_doesNothingWhenCommentNotFound() {
        Long commentId = 99L;
        given(commentRepository.findByIdAndUserId(USER_ID, commentId)).willReturn(Optional.empty());

        commentService.delete(USER_ID, commentId);

        verify(commentRepository, never()).softDelete(any());
    }

    @ParameterizedTest(name = "목표 {1}개 중 {0}번째 댓글 -> {2}")
    @CsvSource({
        "1, 5, FIRST",
        "2, 5, NONE",
        "3, 5, HALF",
        "5, 10, HALF",
        "5, 5, COMPLETED",
        "1, 2, FIRST",
        "2, 2, COMPLETED",
        "2, 3, HALF",
        "1, 1, COMPLETED",
        "50, 100, HALF"
    })
    @DisplayName("다른 사용자의 댓글로 첫 댓글·절반(올림)·목표 달성 시점에 도달하면 해당 알림 이벤트 하나만 발행한다")
    void create_publishesMilestoneEvent(long countAfter, long limit, String expected) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            given(commentCountRepository.findByPostId(POST_ID)).willReturn(
                Optional.of(CommentCount.of(POST_ID, countAfter - 1, limit)));
            given(postsRepository.findById(POST_ID)).willReturn(
                Optional.of(createPost("포스트앨범", "포스트아티스트", false)));

            commentService.create(createOtherUserPrincipal(), otherMusicRequest());

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atMostOnce()).publishEvent(captor.capture());
            Class<?> expectedType = switch (expected) {
                case "FIRST" -> FirstCommentCreatedEvent.class;
                case "HALF" -> HalfCommentReachedEvent.class;
                case "COMPLETED" -> CommentLimitReachedEvent.class;
                default -> null;
            };
            if (expectedType == null) {
                assertThat(captor.getAllValues()).isEmpty();
            } else {
                assertThat(captor.getAllValues()).singleElement().isInstanceOf(expectedType);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("게시글 작성자 본인의 댓글로는 목표에 도달해도 알림 이벤트를 발행하지 않는다")
    void create_doesNotPublishForAuthorComment() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            given(commentCountRepository.findByPostId(POST_ID)).willReturn(
                Optional.of(CommentCount.of(POST_ID, 4L, 5L)));
            given(postsRepository.findById(POST_ID)).willReturn(
                Optional.of(createPost("포스트앨범", "포스트아티스트", false)));

            commentService.create(createUserPrincipal(), otherMusicRequest());

            verify(eventPublisher, never()).publishEvent(any(Object.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private UserPrincipal createOtherUserPrincipal() {
        return new UserPrincipal(OTHER_USER_ID, "other@test.com", "otherUser", "ROLE_USER",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static CommentCreateRequest otherMusicRequest() {
        return new CommentCreateRequest(
            "댓글 내용", "다른앨범", "다른노래", "다른아티스트", "url", "url2", POST_ID);
    }
}
