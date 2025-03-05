package com.cisvan.api.component.akas;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_akas")
public class TitleAkas {

    @EmbeddedId
    private TitleAkasId id; // Clave compuesta (titleId + ordering)

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "region")
    private String region;

    @Column(name = "language")
    private String language;

    @Column(name = "types")
    private String types;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "isoriginaltitle", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOriginalTitle = false;
}
