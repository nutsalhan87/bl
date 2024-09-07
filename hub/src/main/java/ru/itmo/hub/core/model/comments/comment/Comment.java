package ru.itmo.hub.core.model.comments.comment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @SequenceGenerator(name = "comment_seq", sequenceName = "comment_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    private Long id;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private Timestamp creationTimestamp;

    @Column(nullable = false)
    private String value;
}
