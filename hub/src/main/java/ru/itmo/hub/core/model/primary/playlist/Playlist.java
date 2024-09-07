package ru.itmo.hub.core.model.primary.playlist;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "playlist")
public class Playlist {
    @Id
    @SequenceGenerator(name = "playlist_seq", sequenceName = "playlist_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "playlist_seq")
    private Long id;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    private Set<Long> videoIds = new HashSet<>();

    public Playlist(String authorName, String name) {
        this.authorName = authorName;
        this.name = name;
    }
}
