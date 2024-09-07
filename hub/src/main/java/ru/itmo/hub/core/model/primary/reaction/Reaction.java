package ru.itmo.hub.core.model.primary.reaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reaction", uniqueConstraints = {@UniqueConstraint(columnNames = {"videoId", "authorName"})})
public class Reaction {
    @Id
    @SequenceGenerator(name = "reaction_seq", sequenceName = "reaction_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reaction_seq")
    private Long id;

    @Column(nullable = false, name = "videoId")
    private Long videoId;

    @Column(nullable = false, name = "authorName")
    private String authorName;

    @Column(nullable = false)
    private boolean isPositive;
}
