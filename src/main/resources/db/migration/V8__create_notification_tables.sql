CREATE TABLE `device_token`
(
    `id`           bigint       NOT NULL,
    `user_id`      bigint       NOT NULL,
    `token`        varchar(512) NOT NULL,
    `platform`     varchar(20)  NOT NULL,
    `last_used_at` datetime(6) DEFAULT NULL,
    `created_at`   datetime(6) DEFAULT NULL,
    `updated_at`   datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_token_token` (`token`),
    KEY `idx_device_token_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `notification`
(
    `id`          bigint       NOT NULL,
    `receiver_id` bigint       NOT NULL,
    `type`        varchar(30)  NOT NULL,
    `target_id`   bigint      DEFAULT NULL,
    `title`       varchar(100) NOT NULL,
    `body`        varchar(255) NOT NULL,
    `is_read`     bit(1)       NOT NULL,
    `created_at`  datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_notification_receiver_id_id` (`receiver_id`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `notification_setting`
(
    `id`         bigint      NOT NULL,
    `user_id`    bigint      NOT NULL,
    `type`       varchar(30) NOT NULL,
    `enabled`    bit(1)      NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_setting_user_id_type` (`user_id`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

ALTER TABLE users
    DROP COLUMN fcm_token; 