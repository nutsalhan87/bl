package ru.itmo.hub.api.dto;

import lombok.Data;
import ru.itmo.hub.core.model.comments.comment.Comment;

import java.sql.Timestamp;

@Data
public class CommentDto {
    Long id;
    String by;
    Timestamp creationTimestamp; // TODO: разобраться с сериализацией
    String value;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.by = comment.getAuthorName();
        this.creationTimestamp = comment.getCreationTimestamp();
        this.value = comment.getValue();
    }
}
