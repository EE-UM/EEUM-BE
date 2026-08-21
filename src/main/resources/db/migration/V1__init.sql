CREATE TABLE `comment_count` (
                                 `post_id` bigint NOT NULL,
                                 `comment_count` bigint DEFAULT NULL,
                                 `comment_count_limit` bigint DEFAULT NULL,
                                 `version` bigint NOT NULL,
                                 PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `comment_report` (
                                  `id` bigint NOT NULL,
                                  `comment_id` bigint DEFAULT NULL,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `report_reason` varchar(255) DEFAULT NULL,
                                  `reported_user_id` bigint DEFAULT NULL,
                                  `reporter_user_id` bigint DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `comments` (
                            `id` bigint NOT NULL,
                            `album_name` varchar(255) DEFAULT NULL,
                            `apple_music_url` varchar(255) DEFAULT NULL,
                            `artist_name` varchar(255) DEFAULT NULL,
                            `artwork_url` varchar(255) DEFAULT NULL,
                            `song_name` varchar(255) DEFAULT NULL,
                            `content` varchar(255) DEFAULT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `is_deleted` bit(1) DEFAULT NULL,
                            `modifired_at` datetime(6) DEFAULT NULL,
                            `post_id` bigint DEFAULT NULL,
                            `user_id` bigint DEFAULT NULL,
                            `username` varchar(255) DEFAULT NULL,
                            `modified_at` datetime(6) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `idx_post_id_is_deleted_created_at` (`post_id`,`is_deleted`,`created_at`),
                            KEY `idx_post_id_is_deleted_user_id` (`post_id`,`is_deleted`,`user_id`),
                            KEY `idx_user_id_post_id` (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `developer_token` (
                                   `id` bigint NOT NULL,
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `token` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `forbidden_words` (
                                   `id` bigint NOT NULL,
                                   `language` enum('ENGLISH','KOREAN') DEFAULT NULL,
                                   `word` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_language` (`language`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `like_count` (
                              `post_id` bigint NOT NULL,
                              `like_count` bigint DEFAULT NULL,
                              PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `likes` (
                         `id` bigint NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `post_id` bigint DEFAULT NULL,
                         `user_id` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `idx_post_id_user_id` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `post_view_count` (
                                   `post_id` bigint NOT NULL,
                                   `view_count` bigint DEFAULT NULL,
                                   PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `posts` (
                         `id` bigint NOT NULL,
                         `album_name` varchar(255) DEFAULT NULL,
                         `apple_music_url` varchar(255) DEFAULT NULL,
                         `artist_name` varchar(255) DEFAULT NULL,
                         `artwork_url` varchar(255) DEFAULT NULL,
                         `song_name` varchar(255) DEFAULT NULL,
                         `completion_type` enum('AUTO_COMPLETION','MANUAL_COMPLETION') DEFAULT NULL,
                         `content` varchar(255) DEFAULT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `is_completed` bit(1) DEFAULT NULL,
                         `is_deleted` bit(1) DEFAULT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `user_id` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `idx_created_at` (`created_at` DESC),
                         KEY `idx_is_completed_created_at` (`is_completed`,`created_at` DESC),
                         KEY `idx_is_completed_id_created_at` (`is_completed`,`id`,`created_at` DESC),
                         KEY `idx_is_completed_is_deleted_created_at` (`is_completed`,`is_deleted`,`created_at` DESC),
                         KEY `idx_is_deleted_created_at` (`is_deleted`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `posts_comment_count` (
                                       `post_id` bigint NOT NULL,
                                       `current_comment_count` bigint DEFAULT NULL,
                                       `target_comment_count` bigint DEFAULT NULL,
                                       PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `posts_report` (
                                `id` bigint NOT NULL,
                                `created_at` datetime(6) DEFAULT NULL,
                                `post_id` bigint DEFAULT NULL,
                                `report_reason` varchar(255) DEFAULT NULL,
                                `reported_user_id` bigint DEFAULT NULL,
                                `reporter_user_id` bigint DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `users` (
                         `id` bigint NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `email` varchar(255) DEFAULT NULL,
                         `fcm_token` varchar(255) DEFAULT NULL,
                         `is_registered` bit(1) NOT NULL,
                         `nickname` varchar(255) DEFAULT NULL,
                         `provider` varchar(255) DEFAULT NULL,
                         `provider_id` varchar(255) DEFAULT NULL,
                         `role` varchar(255) DEFAULT NULL,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `username` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
