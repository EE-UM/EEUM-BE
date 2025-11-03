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

@Table(name = "PostsReport")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostsReport {

    @Id
    @Tsid
    private Long id;

    private Long postId;

    private Long reportedUserId;

    private Long reporterUserId;

    private String reportReason;

    private LocalDateTime createdAt;

    public static PostsReport of(Long postId, Long reportedUserId, Long reporterUserId, String reportReason) {
        LocalDateTime now = LocalDateTime.now();
        return PostsReport.builder()
                .postId(postId)
                .reportedUserId(reportedUserId)
                .reporterUserId(reporterUserId)
                .reportReason(reportReason)
                .createdAt(now)
                .build();
    }

    @Builder
    public PostsReport(Long postId, Long reportedUserId, Long reporterUserId, String reportReason,
                       LocalDateTime createdAt) {
        this.postId = postId;
        this.reportedUserId = reportedUserId;
        this.reporterUserId = reporterUserId;
        this.reportReason = reportReason;
        this.createdAt = createdAt;
    }
}
