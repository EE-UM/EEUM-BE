package com.eeum.domain.admin.repository;

import java.time.LocalDateTime;

/**
 * 관리자 화면용 게시글 조회 결과. @SoftDelete 필터를 우회하는 네이티브 쿼리라 삭제된 글도 포함한다.
 */
public interface AdminPostRow {

    Long getId();

    String getTitle();

    String getContent();

    Long getUserId();

    String getNickname();

    Boolean getIsCompleted();

    String getCompletionType();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    LocalDateTime getDeleted();

    Long getDeletedBy();

    String getDeleteReason();

    String getSongName();

    String getArtistName();

    String getAlbumName();

    String getAppleMusicUrl();

    Long getCommentCount();

    Long getCommentCountLimit();

    default boolean isRemoved() {
        return getDeleted() != null;
    }

    default String getStatusLabel() {
        if (isRemoved()) {
            return "삭제됨";
        }
        return Boolean.TRUE.equals(getIsCompleted()) ? "완료" : "진행 중";
    }

    default String getDeleteReasonLabel() {
        if (getDeleteReason() == null) {
            return isRemoved() ? "사유 미기록" : null;
        }
        return switch (getDeleteReason()) {
            case "USER" -> "작성자 삭제";
            case "REPORT" -> "신고";
            case "SPAM" -> "스팸 필터";
            case "ADMIN" -> "관리자";
            default -> getDeleteReason();
        };
    }
}
