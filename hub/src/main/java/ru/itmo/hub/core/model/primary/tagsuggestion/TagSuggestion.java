package ru.itmo.hub.core.model.primary.tagsuggestion;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.hub.util.FormatUtils;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tag_suggestion")
public class TagSuggestion {
    public TagSuggestion(Long id, String tagValue, long videoId, boolean isAddSuggestion) {
        this.id = id;
        this.tagValue = tagValue;
        this.videoId = videoId;
        this.isAddSuggestion = isAddSuggestion;
        this.creationTimestamp = Timestamp.from(Instant.now());
    }

    @Id
    @SequenceGenerator(name = "tag_suggestion_seq", sequenceName = "tag_suggestion_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_suggestion_seq")
    private Long id;

    @Column(nullable = false)
    private String tagValue;
    @Column(nullable = false)
    private long videoId;
    /**
     * Whether to add new tag to the video (true value) or remove existing tag from the video (false value)
     */
    @Column(nullable = false)
    private boolean isAddSuggestion;

    @Column(nullable = false)
    private Timestamp creationTimestamp;

    public void setTagValue(String tagValue) {
        this.tagValue = FormatUtils.normalizeTag(tagValue);
    }

}
