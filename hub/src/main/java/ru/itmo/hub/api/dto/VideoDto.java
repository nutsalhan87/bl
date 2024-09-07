package ru.itmo.hub.api.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class VideoDto {
    private Long id;
    private String name;
    private String description;
    private String authorName;
    @Builder.Default private Long views = 0L;
    @Builder.Default private Timestamp posted = Timestamp.from(Instant.now());
    @Builder.Default private Set<String> tags = new HashSet<>();
    @Builder.Default private Long likes = 0L;
    @Builder.Default private Long dislikes = 0L;
}
