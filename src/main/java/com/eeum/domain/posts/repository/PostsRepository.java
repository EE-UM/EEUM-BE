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
                    "where p.is_completed = false " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit);

    @Query(
            value = "select * " +
                    "from posts p " +
                    "where p.id < :lastPostId " +
                    "and p.is_completed = false " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit, @Param("lastPostId") Long lastPostId);

    @Query(
            value = "select * " +
                    "from posts p " +
                    "where p.is_completed = true " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScrollDone(@Param("limit") Long limit);

    @Query(
            value = "select * " +
                    "from posts p " +
                    "where p.id < :lastPostId " +
                    "and p.is_completed = true " +
                    "order by p.id desc limit :limit",
            nativeQuery = true
    )
    List<Posts> findAllInfiniteScrollDone(@Param("limit") Long limit, @Param("lastPostId") Long lastPostId);

    @Query(
            value = "select p.* " +
                    "from posts p " +
                    "left join likes l on p.id = l.post_id " +
                    "where l.user_id = :userId " +
                    "and p.is_deleted = false " +
                    "order by p.created_at desc",
            nativeQuery = true
    )
    List<Posts> findPostsLikedByUserId(@Param("userId") Long userId);

    @Query(
            value = "select distinct p.* " +
                    "from posts p " +
                    "left join comments c on p.id = c.post_id " +
                    "where c.user_id = :userId " +
                    "and p.is_deleted = false " +
                    "order by p.created_at desc",
            nativeQuery = true
    )
    List<Posts> findPostsCommentedByUserId(@Param("userId") Long userId);

    @Query(
            value = "select * from posts p " +
                    "where p.is_completed = false and p.is_deleted = false",
            nativeQuery = true
    )
    List<Posts> findAllActivePosts();
}
