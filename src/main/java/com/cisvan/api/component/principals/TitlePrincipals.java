package com.cisvan.api.component.principals;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_principals")
public class TitlePrincipals {

    @EmbeddedId
    private TitlePrincipalsId id; // Clave compuesta

    @Column(name = "nconst", nullable = false)
    private String nconst; // Referencia a name_basics.nconst

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "job")
    private String job;

    @Column(name = "characters")
    private String characters;
}
