package com.eeum.domain.posts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.CompletionType;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsQueryModelRepository;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class PostsDeletionServiceTest {

    @InjectMocks
    private PostsDeletionService postsDeletionService;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private PostsRandomShakeRepository postsRandomShakeRepository;

    @Mock
    private PostsQueryModelRepository postsQueryModelRepository;

    private static final Long POST_ID = 100L;
    private static final Long ADMIN_ID = 7L;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    private Posts createPost(boolean isCompleted) {
        Album album = Album.of("앨범", "노래", "아티스트", "http://art.url", "http://music.url");
        Posts post = Posts.of("제목", "내용", album, 1L);
        post.updateCompletionType(CompletionType.MANUAL_COMPLETION);
        if (isCompleted) {
            post.updateIsCompleted();
        }
        return post;
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations()
            .forEach(TransactionSynchronization::afterCommit);
    }

    @Test
    @DisplayName("softDelete 는 삭제자와 사유를 기록하고, 커밋 이후에만 랜덤 풀과 쿼리 모델 캐시를 정리한다")
    void softDelete_recordsReasonAndCleansCacheAfterCommit() {
        given(postsRepository.softDelete(eq(POST_ID), eq(ADMIN_ID), eq("ADMIN"), any()))
            .willReturn(1);

        boolean deleted = postsDeletionService.softDelete(POST_ID, ADMIN_ID, DeleteReason.ADMIN);

        assertThat(deleted).isTrue();
        verify(postsRandomShakeRepository, never()).removeCandidate(anyString());
        verify(postsQueryModelRepository, never()).delete(any());

        commit();

        verify(postsRandomShakeRepository).removeCandidate(String.valueOf(POST_ID));
        verify(postsQueryModelRepository).delete(POST_ID);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 softDelete 하면 false 를 반환하고 캐시를 건드리지 않는다")
    void softDelete_alreadyDeleted_noop() {
        given(postsRepository.softDelete(eq(POST_ID), any(), eq("REPORT"), any()))
            .willReturn(0);

        boolean deleted = postsDeletionService.softDelete(POST_ID, 1L, DeleteReason.REPORT);

        assertThat(deleted).isFalse();
        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
    }

    @Test
    @DisplayName("진행 중인 게시글을 복구하면 커밋 이후 랜덤 풀에 다시 넣는다")
    void restore_activePost_addsCandidateAfterCommit() {
        given(postsRepository.restore(POST_ID)).willReturn(1);
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(createPost(false)));

        boolean restored = postsDeletionService.restore(POST_ID);

        assertThat(restored).isTrue();
        verify(postsRandomShakeRepository, never()).addCandidate(any());

        commit();

        verify(postsRandomShakeRepository).addCandidate(any(ShowRandomStoryOnShakeResponse.class));
    }

    @Test
    @DisplayName("완료된 게시글을 복구하면 랜덤 풀에 넣지 않는다")
    void restore_completedPost_doesNotAddCandidate() {
        given(postsRepository.restore(POST_ID)).willReturn(1);
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(createPost(true)));

        boolean restored = postsDeletionService.restore(POST_ID);
        commit();

        assertThat(restored).isTrue();
        verify(postsRandomShakeRepository, never()).addCandidate(any());
    }

    @Test
    @DisplayName("삭제 상태가 아닌 게시글을 복구하면 false 를 반환한다")
    void restore_notDeleted_noop() {
        given(postsRepository.restore(POST_ID)).willReturn(0);

        boolean restored = postsDeletionService.restore(POST_ID);

        assertThat(restored).isFalse();
        verify(postsRepository, never()).findById(any());
    }
}
