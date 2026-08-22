CREATE TABLE `block`
(
    `id`              bigint NOT NULL,
    `blocker_user_id` bigint DEFAULT NULL,
    `blocked_user_id` bigint DEFAULT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `deleted`         datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE posts DROP COLUMN is_deleted;
ALTER TABLE comments DROP COLUMN is_deleted;

