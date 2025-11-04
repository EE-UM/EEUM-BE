package com.eeum.domain.report.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "commentReport")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReport {

    @Id
    @Tsid
    private Long id;

    private Long commentId;

    private Long reportedUserId;

    private Long reporterUserId;

    private String reportReason;

    private LocalDateTime createdAt;

    public static CommentReport of(Long commentId, Long reportedUserId, Long reporterUserId, String reportReason) {
        LocalDateTime now = LocalDateTime.now();
        return CommentReport.builder()
                .commentId(commentId)
                .reportedUserId(reportedUserId)
                .reporterUserId(reporterUserId)
                .reportReason(reportReason)
                .createdAt(now)
                .build();
    }

    @Builder
    public CommentReport(Long commentId, Long reportedUserId, Long reporterUserId, String reportReason,
                         LocalDateTime createdAt) {
        this.commentId = commentId;
        this.reportedUserId = reportedUserId;
        this.reporterUserId = reporterUserId;
        this.reportReason = reportReason;
        this.createdAt = createdAt;
    }
}
