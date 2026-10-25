ALTER TABLE posts
    ADD COLUMN deleted_by    BIGINT      NULL,
    ADD COLUMN delete_reason VARCHAR(20) NULL;

-- 신고는 활성 게시글에만 가능하고 즉시 삭제되므로, 신고 이력이 있는 삭제 게시글은 모두 신고로 삭제된 글이다.
UPDATE posts p
SET p.delete_reason = 'REPORT'
WHERE p.deleted IS NOT NULL
  AND EXISTS (SELECT 1 FROM posts_report pr WHERE pr.post_id = p.id);
