package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.entity.PostsCommentCount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsCommentCountRepository extends JpaRepository<PostsCommentCount, Long> {
    Optional<PostsCommentCount> findByPostId(Long postId);
}
