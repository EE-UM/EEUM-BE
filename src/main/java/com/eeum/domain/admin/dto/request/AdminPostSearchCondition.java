package com.eeum.domain.admin.dto.request;

import org.springframework.util.StringUtils;

public record AdminPostSearchCondition(
    AdminPostStatus status,
    String keyword,
    Long userId,
    int page
) {

    public AdminPostSearchCondition {
        status = status == null ? AdminPostStatus.ALL : status;
        keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        page = Math.max(page, 0);
    }

    /**
     * LIKE 와일드카드(%, _)와 이스케이프 문자를 그대로 검색하도록 이스케이프한 키워드
     */
    public String escapedKeyword() {
        if (keyword == null) {
            return null;
        }
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
