package com.eeum.domain.admin.dto.request;

public enum AdminPostStatus {
    ALL("전체"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    DELETED("삭제됨");

    private final String label;

    AdminPostStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
