CREATE TABLE `batch_job_execution`
(
    `id`              bigint      NOT NULL,
    `job_name`        varchar(50) NOT NULL,
    `status`          varchar(20) NOT NULL,
    `processed_count` int         NOT NULL,
    `failed_count`    int         NOT NULL,
    `error_message`   varchar(500) DEFAULT NULL,
    `started_at`      datetime(6) NOT NULL,
    `finished_at`     datetime(6) NOT NULL,
    PRIMARY KEY (`id`),
    KEY               `idx_batch_job_execution_job_name_started_at` (`job_name`, `started_at`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;