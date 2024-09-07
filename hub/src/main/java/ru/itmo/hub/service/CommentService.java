package ru.itmo.hub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.CommentDto;
import ru.itmo.hub.api.dto.CommentSortingStrategy;
import ru.itmo.hub.core.model.comments.comment.Comment;
import ru.itmo.hub.core.model.comments.comment.CommentRepository;
import ru.itmo.hub.util.SortingUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final VideoService videoService;

    @Transactional
    public List<CommentDto> getComments(Long videoId, CommentSortingStrategy sorting) {
        videoService.findByIdOrThrow(videoId); // проверка на существование видео
        List<Comment> comments = commentRepository.findAllByVideoId(videoId);
        return SortingUtils.sortAndMapComments(comments, sorting);
    }

    @Transactional
    public void createComment(Long videoId, String value, String authorName) {
        videoService.findByIdOrThrow(videoId); // проверка на существование видео
        Timestamp timestamp = Timestamp.from(Instant.now());
        Comment comment = new Comment(null, videoId, authorName, timestamp, value);
        commentRepository.save(comment);
    }
}
