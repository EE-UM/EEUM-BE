package com.eeum.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eeum.domain.comment.repository.CommentRepository;
import com.eeum.domain.posts.entity.Album;
import com.eeum.domain.posts.entity.DeleteReason;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import com.eeum.domain.posts.service.PostsDeletionService;
import com.eeum.domain.report.dto.request.PostsReportRequest;
import com.eeum.domain.report.entity.PostsReport;
import com.eeum.domain.report.exception.AlreadyDeletedException;
import com.eeum.domain.report.repository.CommentReportRepository;
import com.eeum.domain.report.repository.PostsReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private CommentReportRepository commentReportRepository;

    @Mock
    private PostsReportRepository postsReportRepository;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostsDeletionService postsDeletionService;

    private static final Long POST_ID = 100L;
    private static final Long REPORTER_ID = 3L;

    @Test
    @DisplayName("게시글을 신고하면 신고 이력을 저장하고 REPORT 사유로 soft delete 한다")
    void postsReport_savesReportAndSoftDeletesWithReportReason() {
        Album album = Album.of("앨범", "노래", "아티스트", "http://art.url", "http://music.url");
        Posts post = Posts.of("제목", "신고된 내용", album, 1L);
        given(postsRepository.findById(POST_ID)).willReturn(Optional.of(post));

        String content = reportService.postsReport(REPORTER_ID,
            new PostsReportRequest(POST_ID, 1L, "스팸"));

        assertThat(content).isEqualTo("신고된 내용");
        verify(postsReportRepository).save(any(PostsReport.class));
        verify(postsDeletionService).softDelete(post.getId(), REPORTER_ID, DeleteReason.REPORT);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 신고하면 AlreadyDeletedException 이 발생한다")
    void postsReport_alreadyDeleted_throws() {
        given(postsRepository.findById(POST_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.postsReport(REPORTER_ID,
            new PostsReportRequest(POST_ID, 1L, "스팸")))
            .isInstanceOf(AlreadyDeletedException.class);

        verify(postsReportRepository, never()).save(any());
        verify(postsDeletionService, never()).softDelete(any(), any(), any());
    }
}
