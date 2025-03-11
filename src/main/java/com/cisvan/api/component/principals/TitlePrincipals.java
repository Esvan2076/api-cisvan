package com.cisvan.api.component.principals;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_principals")
public class TitlePrincipals {

    @EmbeddedId
    private TitlePrincipalsId id; // Clave compuesta (tconst, ordering)

    @Column(name = "nconst", length = 15)
    private String nconst; // Ahora solo es un campo normal con FK

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "job", length = 200)
    private String job;

    @Column(name = "characters", length = 200)
    private String characters;
}
