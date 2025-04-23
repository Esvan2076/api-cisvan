package com.cisvan.api.domain.akas;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "title_akas")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Akas {

    @EmbeddedId
    private AkasId id;

    @Column(name = "title", columnDefinition = "TEXT", nullable = true)
    private String title;

    @Column(name = "region", length = 10, nullable = true)
    private String region;

    @Column(name = "language", length = 10, nullable = true)
    private String language;

    @Builder.Default
    @Column(name = "types", columnDefinition = "VARCHAR(50)[]", nullable = true)
    private List<String> types = new ArrayList<>();

    @Builder.Default
    @Column(name = "attributes", columnDefinition = "VARCHAR(50)[]", nullable = true)
    private List<String> attributes = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_original_title", columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = true)
    private Boolean isOriginalTitle = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Akas akas)) return false;
        return Objects.equals(id, akas.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
