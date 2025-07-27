package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query(
            value ="select * from posts p where p.is_completed = true order by rand() limit 1",
            nativeQuery = true
    )
    Optional<Posts> findRandomPost();

    @Query(
            value = "select p.id " +
                    "from posts p " +
                    "where p.is_completed = false and p.user_id != :userId",
            nativeQuery = true
    )
    List<Long> findAllIdsIsNotCompletedPosts(Long userId);

    List<Posts> findByUserId(Long userId);

    Optional<Posts> findByIdAndUserId(Long postId, Long userId);

    @Query(
            value = "select * " +
                    "from posts p " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit);

    @Query(
            value = "select * " +
                    "from posts p " +
                    "where p.id < :lastPostId " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit, @Param("lastPostId") Long lastPostId);
}
