package com.eeum.domain.admin.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminTimeFormatterTest {

    @Test
    @DisplayName("UTC 로 저장된 시각은 +9시간 KST 로 표시한다")
    void utcStoredTime_isShownInKst() {
        AdminTimeFormatter formatter = new AdminTimeFormatter(ZoneOffset.UTC);

        assertThat(formatter.format(LocalDateTime.of(2026, 10, 24, 16, 30)))
            .isEqualTo("2026-10-25 01:30");
    }

    @Test
    @DisplayName("KST 로 저장된 시각은 그대로 표시하고, null 은 '-' 로 표시한다")
    void kstStoredTime_isShownAsIs() {
        AdminTimeFormatter formatter = new AdminTimeFormatter(ZoneId.of("Asia/Seoul"));

        assertThat(formatter.format(LocalDateTime.of(2026, 10, 25, 1, 30)))
            .isEqualTo("2026-10-25 01:30");
        assertThat(formatter.format(null)).isEqualTo("-");
    }
}
