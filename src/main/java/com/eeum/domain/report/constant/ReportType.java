package com.eeum.domain.report.constant;

public enum ReportType {
    POSTS("게시글(플레이리스트)"), COMMENT("댓글");

    private final String status;

    ReportType(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
