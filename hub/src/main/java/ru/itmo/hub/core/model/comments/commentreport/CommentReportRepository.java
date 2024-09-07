package ru.itmo.hub.core.model.comments.commentreport;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import ru.itmo.hub.core.model.comments.comment.Comment;

import java.util.List;

public interface CommentReportRepository extends CrudRepository<CommentReport, Long> {
    boolean existsByComment(Comment comment);
    @NonNull
    List<CommentReport> findAll();
    @NonNull
    @Query("select cr.comment from CommentReport cr")
    List<Comment> findAllComments();
    void deleteByComment(Comment comment);
}
