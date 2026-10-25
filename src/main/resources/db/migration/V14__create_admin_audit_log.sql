CREATE TABLE `admin_audit_log`
(
    `id`            bigint       NOT NULL,
    `admin_user_id` bigint       NOT NULL,
    `action`        varchar(30)  NOT NULL,
    `target_type`   varchar(30)  NOT NULL,
    `target_id`     bigint       NOT NULL,
    `reason`        varchar(500) NOT NULL,
    `ip`            varchar(45) DEFAULT NULL,
    `created_at`    datetime(6)  NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_admin_audit_log_target` (`target_type`, `target_id`),
    KEY `idx_admin_audit_log_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
