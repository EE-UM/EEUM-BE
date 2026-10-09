ALTER TABLE `device_token`
    RENAME COLUMN `token` TO `fcm_token`,
    RENAME INDEX `uk_device_token_token` TO `idx_fcm_token`;

ALTER TABLE `notification`
    RENAME COLUMN `receiver_id` TO `user_id`,
    RENAME INDEX `idx_notification_receiver_id_id` TO `idx_notification_user_id_id`,
    ADD COLUMN `target_type` varchar(20) DEFAULT NULL AFTER `body`,
    ADD COLUMN `status`      varchar(20) NOT NULL DEFAULT 'SENT' AFTER `target_id`,
    ADD COLUMN `sent_at`     datetime(6) DEFAULT NULL AFTER `is_read`,
    ADD COLUMN `read_at`     datetime(6) DEFAULT NULL AFTER `created_at`;

-- 기존 행은 SENT로 채워 재발행 대상에서 제외하고, 이후 행은 애플리케이션에서 status를 지정한다.
ALTER TABLE `notification`
    ALTER COLUMN `status` DROP DEFAULT;

CREATE INDEX `idx_user_id_created_at` ON `notification` (`user_id`, `created_at`);
CREATE INDEX `idx_user_id_is_read` ON `notification` (`user_id`, `is_read`);
CREATE INDEX `idx_target_type_target_id` ON `notification` (`target_type`, `target_id`);
CREATE INDEX `idx_status_created_at` ON `notification` (`status`, `created_at`);

CREATE TABLE `dead_letter_message`
(
    `id`            bigint       NOT NULL,
    `queue_name`    varchar(100) DEFAULT NULL,
    `payload`       text,
    `error_message` text,
    `try_count`     int          NOT NULL,
    `resolved`      bit(1)       NOT NULL,
    `failed_at`     datetime(6)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_dead_letter_message_resolved_failed_at` (`resolved`, `failed_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
