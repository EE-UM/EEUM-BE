package com.eeum.domain.admin.repository;

import com.eeum.domain.admin.dto.response.AdminStatRows.BatchJobRow;
import com.eeum.domain.admin.dto.response.AdminStatRows.RankedPost;
import com.eeum.domain.admin.dto.response.AdminStatRows.RecentAdminAction;
import com.eeum.domain.admin.dto.response.AdminStatRows.RecentReport;
import com.eeum.domain.admin.dto.response.AdminStatRows.RedisSwitchRow;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 관리자 대시보드 집계 전용 읽기 쿼리. 여러 테이블을 가로지르는 통계라 엔티티 대신 JDBC 로 조회한다.
 * soft delete 된 행을 직접 다루므로 각 쿼리에서 deleted 조건을 명시한다.
 */
@Repository
@RequiredArgsConstructor
public class AdminStatsRepository {

    /**
     * 일별 생성 수를 집계할 수 있는 테이블. SQL 에 테이블명이 직접 들어가므로 이 enum 으로만 지정한다.
     */
    public enum DailySource {
        USERS("users"),
        POSTS("posts"),
        COMMENTS("comments"),
        LIKES("likes"),
        POST_REPORTS("posts_report"),
        COMMENT_REPORTS("comment_report");

        private final String table;

        DailySource(String table) {
            this.table = table;
        }
    }

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * from(저장 시간대) 이후 생성된 행을 KST 날짜별로 센다. 삭제된 행도 생성 이력이므로 포함한다.
     */
    public Map<LocalDate, Long> dailyCounts(DailySource source, LocalDateTime from,
        int offsetSeconds) {
        String sql = """
            SELECT DATE_FORMAT(DATE_ADD(created_at, INTERVAL :offset SECOND), '%%Y-%%m-%%d') AS d,
                   COUNT(*) AS c
            FROM %s
            WHERE created_at >= :from
            GROUP BY d
            """.formatted(source.table);
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        jdbc.query(sql, new MapSqlParameterSource()
                .addValue("offset", offsetSeconds)
                .addValue("from", from),
            rs -> {
                result.put(LocalDate.parse(rs.getString("d")), rs.getLong("c"));
            });
        return result;
    }

    public long countUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    public Map<String, Long> countUsersByProvider() {
        return groupCount("SELECT COALESCE(provider, 'UNKNOWN') AS k, COUNT(*) AS c "
            + "FROM users GROUP BY k ORDER BY c DESC");
    }

    public Map<String, Long> countUsersByStatus() {
        return groupCount("SELECT COALESCE(status, 'UNKNOWN') AS k, COUNT(*) AS c "
            + "FROM users GROUP BY k ORDER BY c DESC");
    }

    /**
     * 게시글 상태별 수. 키: IN_PROGRESS, COMPLETED, DELETED_{USER|REPORT|SPAM|ADMIN|UNKNOWN}
     */
    public Map<String, Long> countPostsByState() {
        return groupCount("""
            SELECT CASE
                       WHEN deleted IS NOT NULL THEN CONCAT('DELETED_', COALESCE(delete_reason, 'UNKNOWN'))
                       WHEN is_completed = true THEN 'COMPLETED'
                       ELSE 'IN_PROGRESS'
                   END AS k,
                   COUNT(*) AS c
            FROM posts
            GROUP BY k
            ORDER BY c DESC
            """);
    }

    public Map<String, Long> countActivePostsByCompletionType() {
        return groupCount("SELECT COALESCE(completion_type, 'UNKNOWN') AS k, COUNT(*) AS c "
            + "FROM posts WHERE deleted IS NULL GROUP BY k ORDER BY c DESC");
    }

    public long countActiveComments() {
        return count("SELECT COUNT(*) FROM comments WHERE deleted IS NULL");
    }

    public long countLikes() {
        return count("SELECT COUNT(*) FROM likes");
    }

    public long sumViews() {
        return count("SELECT COALESCE(SUM(view_count), 0) FROM post_view_count");
    }

    public long countActiveBlocks() {
        return count("SELECT COUNT(*) FROM block WHERE deleted IS NULL");
    }

    public long countDeviceTokens() {
        return count("SELECT COUNT(*) FROM device_token");
    }

    public long countPostReports() {
        return count("SELECT COUNT(*) FROM posts_report");
    }

    public long countCommentReports() {
        return count("SELECT COUNT(*) FROM comment_report");
    }

    public List<RankedPost> topPostsByComments(int limit) {
        return rankedPosts("comment_count cc ON cc.post_id = p.id", "cc.comment_count", limit);
    }

    public List<RankedPost> topPostsByLikes(int limit) {
        return rankedPosts("like_count lc ON lc.post_id = p.id", "lc.like_count", limit);
    }

    public List<RankedPost> topPostsByViews(int limit) {
        return rankedPosts("post_view_count vc ON vc.post_id = p.id", "vc.view_count", limit);
    }

    public List<RecentReport> recentReports(int limit) {
        return jdbc.query("""
                SELECT r.* FROM (
                    SELECT 'POST' AS type, pr.post_id AS target_id, pr.post_id AS post_id,
                           p.title AS content, pr.report_reason AS reason,
                           pr.reporter_user_id AS reporter_user_id, pr.created_at AS created_at
                    FROM posts_report pr
                    LEFT JOIN posts p ON p.id = pr.post_id
                    UNION ALL
                    SELECT 'COMMENT', cr.comment_id, c.post_id, c.content, cr.report_reason,
                           cr.reporter_user_id, cr.created_at
                    FROM comment_report cr
                    LEFT JOIN comments c ON c.id = cr.comment_id
                ) r
                ORDER BY r.created_at DESC
                LIMIT :limit
                """,
            new MapSqlParameterSource("limit", limit),
            (rs, i) -> new RecentReport(
                rs.getString("type"),
                nullableLong(rs, "target_id"),
                nullableLong(rs, "post_id"),
                rs.getString("content"),
                rs.getString("reason"),
                nullableLong(rs, "reporter_user_id"),
                localDateTime(rs, "created_at")));
    }

    public Map<String, Long> countNotificationsByStatusSince(LocalDateTime since) {
        Map<String, Long> result = new LinkedHashMap<>();
        jdbc.query("SELECT status AS k, COUNT(*) AS c FROM notification "
                + "WHERE created_at >= :since GROUP BY status",
            new MapSqlParameterSource("since", since),
            rs -> {
                result.put(rs.getString("k"), rs.getLong("c"));
            });
        return result;
    }

    public List<RedisSwitchRow> redisSwitches() {
        return jdbc.query("SELECT feature, enabled, updated_at FROM redis_switch ORDER BY feature",
            (rs, i) -> new RedisSwitchRow(
                rs.getString("feature"),
                rs.getBoolean("enabled"),
                localDateTime(rs, "updated_at")));
    }

    /**
     * 작업별 가장 최근 실행 1건. (실행 이력은 처리 건이 있는 실행만 기록된다)
     */
    public List<BatchJobRow> latestBatchJobs() {
        return jdbc.query("""
                SELECT b.job_name, b.status, b.processed_count, b.failed_count, b.error_message,
                       b.started_at, b.finished_at
                FROM batch_job_execution b
                JOIN (SELECT job_name, MAX(started_at) AS started_at
                      FROM batch_job_execution
                      GROUP BY job_name) latest
                  ON latest.job_name = b.job_name AND latest.started_at = b.started_at
                ORDER BY b.started_at DESC
                """,
            (rs, i) -> new BatchJobRow(
                rs.getString("job_name"),
                rs.getString("status"),
                rs.getInt("processed_count"),
                rs.getInt("failed_count"),
                rs.getString("error_message"),
                localDateTime(rs, "started_at"),
                localDateTime(rs, "finished_at")));
    }

    public long countFailedBatchJobsSince(LocalDateTime since) {
        Long count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM batch_job_execution WHERE status = 'FAILED' AND started_at >= :since",
            new MapSqlParameterSource("since", since), Long.class);
        return count == null ? 0 : count;
    }

    public List<RecentAdminAction> recentAdminActions(int limit) {
        return jdbc.query("""
                SELECT a.created_at, a.admin_user_id, u.nickname, a.action, a.target_id, a.reason
                FROM admin_audit_log a
                LEFT JOIN users u ON u.id = a.admin_user_id
                ORDER BY a.id DESC
                LIMIT :limit
                """,
            new MapSqlParameterSource("limit", limit),
            (rs, i) -> new RecentAdminAction(
                localDateTime(rs, "created_at"),
                rs.getLong("admin_user_id"),
                rs.getString("nickname"),
                rs.getString("action"),
                rs.getLong("target_id"),
                rs.getString("reason")));
    }

    private List<RankedPost> rankedPosts(String join, String valueColumn, int limit) {
        String sql = """
            SELECT p.id, p.title, u.nickname, %s AS v, p.is_completed
            FROM posts p
            JOIN %s
            LEFT JOIN users u ON u.id = p.user_id
            WHERE p.deleted IS NULL AND %s > 0
            ORDER BY v DESC, p.id DESC
            LIMIT :limit
            """.formatted(valueColumn, join, valueColumn);
        return jdbc.query(sql, new MapSqlParameterSource("limit", limit),
            (rs, i) -> new RankedPost(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("nickname"),
                rs.getLong("v"),
                rs.getBoolean("is_completed")));
    }

    private long count(String sql) {
        Long count = jdbc.queryForObject(sql, Map.of(), Long.class);
        return count == null ? 0 : count;
    }

    private Map<String, Long> groupCount(String sql) {
        Map<String, Long> result = new LinkedHashMap<>();
        jdbc.query(sql, rs -> {
            result.put(rs.getString("k"), rs.getLong("c"));
        });
        return result;
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    // 드라이버의 시간대 변환 없이 저장된 벽시계 시각 그대로 읽는다 (JPA 엔티티와 동일한 기준)
    private static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }
}
