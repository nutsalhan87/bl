package ru.itmo.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@With
@Builder
public class Video {
    @Setter(AccessLevel.NONE)
    private final Long id;
    private String name;
    private String description;
    private String authorName;
    @Builder.Default
    private Long views = 0L;
    @Builder.Default
    private Timestamp posted = Timestamp.from(Instant.now());
    @Builder.Default
    private Set<Long> tagIds = new HashSet<>();
    @Builder.Default
    private Set<Long> reactionIds = new HashSet<>();

    @JsonCreator
    public Video(@JsonProperty("id") Long id,
                 @JsonProperty("name") String name,
                 @JsonProperty("description") String description,
                 @JsonProperty("authorName") String authorName,
                 @JsonProperty("views") Long views,
                 @JsonProperty("posted") Timestamp posted,
                 @JsonProperty("tagIds") Set<Long> tagIds,
                 @JsonProperty("reactionIds") Set<Long> reactionIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.authorName = authorName;
        this.views = views;
        this.posted = posted;
        this.tagIds = tagIds;
        this.reactionIds = reactionIds;
    }
}