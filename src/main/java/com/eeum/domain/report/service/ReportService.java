package com.eeum.domain.report.service;

import com.eeum.domain.comment.entity.Comment;
import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.report.dto.request.CommentReportRequest;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.entity.CommentReport;
import com.eeum.domain.report.entity.PostsReport;
import com.eeum.domain.report.exception.AlreadyDeletedException;
import com.eeum.domain.report.repository.CommentReportRepository;
import com.eeum.domain.report.repository.PostsReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReportService {

    private final CommentReportRepository commentReportRepository;
    private final PostsReportRepository postsReportRepository;
    private final PostsRepository postsRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public String postsReport(Long reporterUserId, PostsReportRequest postsReportRequest) {
        Posts posts = postsRepository.findById(postsReportRequest.postId())
                .orElseThrow(AlreadyDeletedException::new);

        PostsReport postsReport = PostsReport.of(postsReportRequest.postId(), postsReportRequest.reportedUserId(),
                reporterUserId,
                postsReportRequest.reportReason());

        postsReportRepository.save(postsReport);
        posts.softDelete();

        return posts.getContent();
    }

    @Transactional
    public String commentReport(Long reporterUserId, CommentReportRequest commentReportRequest) {
        Comment comment = commentRepository.findById(commentReportRequest.commentId())
                .orElseThrow(AlreadyDeletedException::new);

        CommentReport commentReport = CommentReport.of(commentReportRequest.commentId(),
                commentReportRequest.reportedUserId(),
                reporterUserId,
                commentReportRequest.reportReason());

        commentReportRepository.save(commentReport);
        comment.softDelete();

        return comment.getContent();
    }
}
