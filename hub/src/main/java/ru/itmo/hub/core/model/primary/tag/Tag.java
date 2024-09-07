package ru.itmo.hub.core.model.primary.tag;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.hub.util.FormatUtils;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tag")
public class Tag {
    @Id
    @SequenceGenerator(name = "tag_seq", sequenceName = "tag_sequence", allocationSize = 5)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    private Long id;

    @Column(unique = true)
    private String value;

    public void setValue(String value) {
        this.value = FormatUtils.normalizeTag(value);
    }
}
