package ru.itmo.hub.core.model.comments.comment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import ru.itmo.hub.exception.ServiceException;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAllByVideoId(Long videoId);

    default Comment getCommentByIdOrThrow(@NonNull Long commentId) {
        return this.findById(commentId).orElseThrow(() -> ServiceException.commentNotFound(commentId));
    }
}
