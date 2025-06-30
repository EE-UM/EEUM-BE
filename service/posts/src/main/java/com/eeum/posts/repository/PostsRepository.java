package com.eeum.posts.repository;

import com.eeum.posts.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query(
            value ="select * from posts p where p.is_completed = true order by rand() limit 1",
            nativeQuery = true
    )
    Optional<Posts> findRandomPost();

    @Query(
            value = "select p.id from posts p where p.is_completed = false and p.user_id != :userId",
            nativeQuery = true
    )
    List<Long> findAllIdsIsNotCompletedPosts(Long userId);
}
