package com.eeum.domain.admin.repository;

import com.eeum.domain.posts.entity.Posts;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 관리자 게시글 조회 전용 (읽기 전용). 삭제·복구는 PostsDeletionService 를 사용한다.
 */
public interface AdminPostQueryRepository extends Repository<Posts, Long> {

    String SELECT_COLUMNS = """
        SELECT p.id AS id, p.title AS title, p.content AS content, p.user_id AS userId,
               u.nickname AS nickname, p.is_completed AS isCompleted,
               p.completion_type AS completionType, p.created_at AS createdAt,
               p.updated_at AS updatedAt, p.deleted AS deleted, p.deleted_by AS deletedBy,
               p.delete_reason AS deleteReason, p.song_name AS songName,
               p.artist_name AS artistName, p.album_name AS albumName,
               p.apple_music_url AS appleMusicUrl, cc.comment_count AS commentCount,
               cc.comment_count_limit AS commentCountLimit
        FROM posts p
        LEFT JOIN users u ON u.id = p.user_id
        LEFT JOIN comment_count cc ON cc.post_id = p.id
        """;

    String SEARCH_CONDITION = """
        WHERE (:status = 'ALL'
               OR (:status = 'IN_PROGRESS' AND p.deleted IS NULL AND COALESCE(p.is_completed, false) = false)
               OR (:status = 'COMPLETED' AND p.deleted IS NULL AND p.is_completed = true)
               OR (:status = 'DELETED' AND p.deleted IS NOT NULL))
          AND (:userId IS NULL OR p.user_id = :userId)
          AND (:keyword IS NULL
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """;

    @Query(value = SELECT_COLUMNS + SEARCH_CONDITION + " ORDER BY p.id DESC",
        countQuery = "SELECT COUNT(*) FROM posts p " + SEARCH_CONDITION,
        nativeQuery = true)
    Page<AdminPostRow> search(@Param("status") String status,
        @Param("userId") Long userId,
        @Param("keyword") String keyword,
        Pageable pageable);

    @Query(value = SELECT_COLUMNS + " WHERE p.id = :id", nativeQuery = true)
    Optional<AdminPostRow> findRowById(@Param("id") Long id);
}
