package com.cisvan.api.domain.name;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "name_basics")
public class Name {

    private static final String IMAGE_BASE_URL = "https://media.themoviedb.org/t/p/w300_and_h450_bestv2";
    private static final String IMAGE_DEFAULT_URL = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";

    @Id
    @Column(name = "nconst", length = 15, nullable = false)
    private String nconst;

    @Column(name = "primary_name", nullable = false)
    private String primaryName;

    @Column(name = "birth_year")
    private Short birthYear; // Cambiado de Integer a Short para coincidir con SMALLINT

    @Column(name = "death_year")
    private Short deathYear; // Cambiado de Integer a Short para coincidir con SMALLINT

    @Column(name = "primary_profession", columnDefinition = "TEXT[]")
    private List<String> primaryProfession; // Cambiado a List<String> para reflejar el array en PostgreSQL

    @Column(name = "known_for_titles", columnDefinition = "TEXT[]")
    private List<String> knownForTitles = new ArrayList<>(); // Cambiado a List<String> para reflejar el array en PostgreSQL

    @Column(name = "image_url", length = 100)
    private String imageUrl;

    // Modificar la URL al obtener el valor
    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return IMAGE_BASE_URL + imageUrl;
        }
        return IMAGE_DEFAULT_URL;
    }
}
