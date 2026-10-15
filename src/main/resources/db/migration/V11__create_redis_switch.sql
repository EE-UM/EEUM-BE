CREATE TABLE `redis_switch`
(
    `feature`    varchar(50) NOT NULL,
    `enabled`    bit(1)      NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    PRIMARY KEY (`feature`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `redis_switch` (`feature`, `enabled`, `updated_at`)
VALUES ('SHAKE_POOL', b'1', NOW(6)),
       ('BLOCK_CACHE', b'1', NOW(6)),
       ('SPAM_PUBSUB', b'1', NOW(6));
