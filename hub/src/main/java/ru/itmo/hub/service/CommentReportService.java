package ru.itmo.hub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.CommentDto;
import ru.itmo.hub.core.model.comments.comment.Comment;
import ru.itmo.hub.core.model.comments.comment.CommentRepository;
import ru.itmo.hub.core.model.comments.commentreport.CommentReport;
import ru.itmo.hub.core.model.comments.commentreport.CommentReportRepository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentReportService {
    private final CommentReportRepository commentReportRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void report(Long commentId) {
        var comment = commentRepository.getCommentByIdOrThrow(commentId);
        if (!commentReportRepository.existsByComment(comment)) {
            var commentReport = new CommentReport(comment);
            commentReportRepository.save(commentReport);
        }
    }

    @Transactional
    public List<CommentDto> getUncheckedComments() {
        return commentReportRepository
                .findAllComments()
                .stream()
                .map(CommentDto::new)
                .toList();
    }

    @Transactional
    public void approveComment(Long commentId) {
        Comment comment = commentRepository.getCommentByIdOrThrow(commentId);
        commentReportRepository.deleteByComment(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.getCommentByIdOrThrow(commentId);
        commentReportRepository.deleteByComment(comment);
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteOldReports(Duration timeAgo) {
        var dayAgoTimestamp = Timestamp.from(Instant.now().minus(timeAgo));
        var reportIdsToDelete = commentReportRepository.findAll()
                .stream()
                .filter(report -> report.getCreationTimestamp().before(dayAgoTimestamp))
                .map(CommentReport::getId)
                .toList();
        commentReportRepository.deleteAllById(reportIdsToDelete);
    }
}
