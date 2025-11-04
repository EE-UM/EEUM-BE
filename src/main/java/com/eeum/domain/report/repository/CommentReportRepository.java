package com.eeum.domain.report.repository;

import com.eeum.domain.report.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
}
