package com.eeum.domain.web.service;

import com.eeum.domain.posts.dto.response.ShowRandomStoryOnShakeResponse;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRandomShakeRepository;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.posts.service.PostsService;
import com.eeum.domain.web.dto.WebPostView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebPostService {

    private final PostsService postsService;
    private final PostsRandomShakeRepository postsRandomShakeRepository;
    private final PostsRepository postsRepository;

    /**
     * 비로그인 방문자에게 보여줄 진행 중인 사연 하나를 고른다. 흔들기와 같은 Redis 랜덤 풀을 쓰고, 풀이 비었거나 Redis 장애면
     * DB 에서 고른다.
     */
    public Optional<Long> pickRandomPostId() {
        return postsRandomShakeRepository
            .pickRandomExcludingInOneShot(Set.of(), PostsService.MAX_SHAKE_ATTEMPTS)
            .map(ShowRandomStoryOnShakeResponse::postId)
            // 차단 목록이 없으므로 아무 사용자와도 겹치지 않는 0 을 넘긴다 (NOT IN 빈 목록 회피, PostsService 와 동일)
            .or(() -> postsRepository.findRandomActivePostExcludingUserIds(List.of(0L))
                .map(Posts::getId));
    }

    public WebPostView read(Long postId) {
        return WebPostView.from(postsService.read(null, postId));
    }
}
