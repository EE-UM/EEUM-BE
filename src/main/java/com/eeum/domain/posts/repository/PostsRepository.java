package com.eeum.domain.posts.repository;

import com.eeum.domain.posts.entity.Posts;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostsRepository extends JpaRepository<Posts, Long> {

    List<Posts> findByUserId(Long userId);

    long countByIsCompletedTrue();

    @Query(value = """
                SELECT COUNT(*) FROM posts
                WHERE deleted IS NOT NULL AND delete_reason = :deleteReason
        """, nativeQuery = true)
    long countDeletedByReason(@Param("deleteReason") String deleteReason);

    Optional<Posts> findByIdAndUserId(Long postId, Long userId);

    @Query(
        value = "select * " +
            "from posts p " +
            "where p.is_completed = false " +
            "and p.deleted is null " +
            "and not exists (" +
            "select 1 from block b " +
            "where b.blocker_user_id = :currentUserId " +
            "and b.blocked_user_id = p.user_id " +
            "and b.deleted is null " +
            ") " +
            "order by p.created_at desc limit :limit",
        nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit,
        @Param("currentUserId") Long currentUserId);

    @Query(
        value = "select * " +
            "from posts p " +
            "where p.id < :lastPostId " +
            "and p.is_completed = false " +
            "and p.deleted is null " +
            "and not exists (" +
            "select 1 from block b " +
            "where b.blocker_user_id = :currentUserId " +
            "and b.blocked_user_id = p.user_id " +
            "and b.deleted is null " +
            ") " +
            "order by p.id, p.created_at desc limit :limit",
        nativeQuery = true
    )
    List<Posts> findAllInfiniteScroll(@Param("limit") Long limit,
        @Param("lastPostId") Long lastPostId,
        @Param("currentUserId") Long currentUserId);

    @Query(
        value = "select * " +
            "from posts p " +
            "where p.is_completed = true " +
            "and p.deleted is null " +
            "and not exists (" +
            "select 1 from block b " +
            "where b.blocker_user_id = :currentUserId " +
            "and b.blocked_user_id = p.user_id " +
            "and b.deleted is null " +
            ") " +
            "order by p.created_at desc limit :limit",
        nativeQuery = true
    )
    List<Posts> findAllInfiniteScrollDone(@Param("limit") Long limit,
        @Param("currentUserId") Long currentUserId);

    @Query(
        value = "select * " +
            "from posts p " +
            "where p.id < :lastPostId " +
            "and p.is_completed = true " +
            "and p.deleted is null " +
            "and not exists (" +
            "select 1 from block b " +
            "where b.blocker_user_id = :currentUserId " +
            "and b.blocked_user_id = p.user_id " +
            "and b.deleted is null " +
            ") " +
            "order by p.id, p.created_at desc limit :limit",
        nativeQuery = true
    )
    List<Posts> findAllInfiniteScrollDone(@Param("limit") Long limit,
        @Param("lastPostId") Long lastPostId,
        @Param("currentUserId") Long currentUserId);

    @Query(
        value = "select p.* " +
            "from posts p " +
            "left join likes l on p.id = l.post_id " +
            "where l.user_id = :userId " +
            "and p.deleted is null " +
            "order by p.created_at desc",
        nativeQuery = true
    )
    List<Posts> findPostsLikedByUserId(@Param("userId") Long userId);

    @Query(
        value = "select distinct p.* " +
            "from (select * from posts where deleted is null) p " +
            "inner join comments c on p.id = c.post_id " +
            "where c.user_id = :userId ",
        nativeQuery = true
    )
    List<Posts> findPostsCommentedByUserId(@Param("userId") Long userId);

    @Query(
        value = "select * from posts p " +
            "where p.is_completed = false " +
            "and p.deleted is null",
        nativeQuery = true
    )
    List<Posts> findAllActivePosts();

    @Query(
        value = """
                  select * from posts p 
                  where p.is_completed = false 
                  and p.deleted is null and p.user_id not in (:excludedUserIds)
                    order by rand() limit 1
            """,
        nativeQuery = true
    )
    Optional<Posts> findRandomActivePostExcludingUserIds(
        @Param("excludedUserIds") Collection<Long> excludedUserIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
                UPDATE posts
                SET deleted = :now, deleted_by = :deletedBy, delete_reason = :deleteReason
                WHERE id = :id AND deleted IS NULL
        """, nativeQuery = true)
    int softDelete(@Param("id") Long id,
        @Param("deletedBy") Long deletedBy,
        @Param("deleteReason") String deleteReason,
        @Param("now") LocalDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
                UPDATE posts
                SET deleted = NULL, deleted_by = NULL, delete_reason = NULL
                WHERE id = :id AND deleted IS NOT NULL
        """, nativeQuery = true)
    int restore(@Param("id") Long id);
}
