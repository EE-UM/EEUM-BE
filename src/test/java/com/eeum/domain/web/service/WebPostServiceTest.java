package com.eeum.domain.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.posts.service.PostsService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebPostServiceTest {

    @Mock
    private PostsService postsService;
    @Mock
    private PostsRandomShakeRepository postsRandomShakeRepository;
    @Mock
    private PostsRepository postsRepository;
    @InjectMocks
    private WebPostService webPostService;

    @Test
    void 랜덤_풀에서_사연을_고른다() {
        given(postsRandomShakeRepository.pickRandomExcludingInOneShot(Set.of(),
            PostsService.MAX_SHAKE_ATTEMPTS))
            .willReturn(Optional.of(new ShowRandomStoryOnShakeResponse(5L, 1L, "t", "c")));

        assertThat(webPostService.pickRandomPostId()).contains(5L);
        verify(postsRepository, never()).findRandomActivePostExcludingUserIds(List.of(0L));
    }

    @Test
    void 랜덤_풀이_비면_DB에서_고른다() {
        given(postsRandomShakeRepository.pickRandomExcludingInOneShot(anySet(), anyInt()))
            .willReturn(Optional.empty());
        Posts post = mock(Posts.class);
        given(post.getId()).willReturn(9L);
        given(postsRepository.findRandomActivePostExcludingUserIds(List.of(0L)))
            .willReturn(Optional.of(post));

        assertThat(webPostService.pickRandomPostId()).contains(9L);
    }

    @Test
    void 진행_중인_사연이_없으면_비어_있다() {
        given(postsRandomShakeRepository.pickRandomExcludingInOneShot(anySet(), anyInt()))
            .willReturn(Optional.empty());
        given(postsRepository.findRandomActivePostExcludingUserIds(List.of(0L)))
            .willReturn(Optional.empty());

        assertThat(webPostService.pickRandomPostId()).isEmpty();
    }
}
