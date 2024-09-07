package ru.itmo.hub.core.model.comments.commentreport;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.hub.core.model.comments.comment.Comment;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "comment_report")
public class CommentReport {
    @Id
    @SequenceGenerator(name = "comment_report_seq", sequenceName = "comment_report_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_report_seq")
    private Long id;

    @JoinColumn(unique = true, nullable = false)
    @OneToOne(optional = false)
    private Comment comment;

    @Column(nullable = false)
    private Timestamp creationTimestamp;

    public CommentReport(Comment comment) {
        this.id = null;
        this.comment = comment;
        this.creationTimestamp = Timestamp.from(Instant.now());
    }
}
