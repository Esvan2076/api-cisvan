package com.cisvan.api.domain.akas;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "title_akas")
public class Akas {

    @EmbeddedId
    private AkasId id; // Clave compuesta (tconst + ordering)

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "region", length = 10)
    private String region;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "types", columnDefinition = "TEXT[]")
    private List<String> types; // Mapeo de array a List<String>

    @Column(name = "attributes", columnDefinition = "TEXT[]")
    private List<String> attributes; // Mapeo de array a List<String>

    @Column(name = "is_original_title", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOriginalTitle = false;
}
