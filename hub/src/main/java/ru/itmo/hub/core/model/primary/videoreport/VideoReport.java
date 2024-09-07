package ru.itmo.hub.core.model.primary.videoreport;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "video_report")
public class VideoReport {
    @Id
    @SequenceGenerator(name = "video_report_seq", sequenceName = "video_report_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_report_seq")
    @Column(name = "video_report_id")
    private Long id;

    @Column(name = "video_id", unique = true, nullable = false)
    private Long videoId;
}
