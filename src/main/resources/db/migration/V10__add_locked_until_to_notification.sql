ALTER TABLE `notification`
    ADD COLUMN `locked_until` datetime(6) DEFAULT NULL;

CREATE INDEX `idx_status_locked_until` ON `notification` (`status`, `locked_until`);

