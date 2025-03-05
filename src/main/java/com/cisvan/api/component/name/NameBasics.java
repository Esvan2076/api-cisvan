package com.cisvan.api.component.name;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "name_basics")
public class NameBasics {

    private static final String IMAGE_BASE_URL = "https://media.themoviedb.org/t/p/w300_and_h450_bestv2";

    @Id
    @Column(name = "nconst", nullable = false)
    private String nconst;

    @Column(name = "primaryname", nullable = false)
    private String primaryName;

    @Column(name = "birthyear")
    private Integer birthYear;

    @Column(name = "deathyear")
    private Integer deathYear;

    @Column(name = "primaryprofession")
    private String primaryProfession;

    @Column(name = "knownfortitles")
    private String knownForTitles;

    @Column(name = "imageurl")
    private String imageUrl;

    // Modificar la URL al obtener el valor
    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return IMAGE_BASE_URL + imageUrl;
        }
        return null; // O podrías devolver una imagen por defecto si es necesario
    }
}
