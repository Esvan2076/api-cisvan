package com.cisvan.api.component.title;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_basics")
public class TitleBasics {

    private static final String IMAGE_BASE_URL = "https://m.media-amazon.com/images/M/";
    private static final String IMAGE_SUFFIX = "._V1_SX500.jpg";
    private static final String IMAGE_DEFAULT_URL = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "title_type", nullable = false, length = 20)
    private String titleType;

    @Column(name = "primary_title", nullable = false, length = 255)
    private String primaryTitle;

    @Column(name = "original_title", nullable = false, length = 255)
    private String originalTitle;

    @Column(name = "is_adult", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdult = false;

    @Column(name = "start_year")
    private Short startYear; // Cambiado a Short para coincidir con SMALLINT en PostgreSQL

    @Column(name = "end_year")
    private Short endYear; // Cambiado a Short para coincidir con SMALLINT en PostgreSQL

    @Column(name = "runtime_minutes", columnDefinition = "SMALLINT DEFAULT 0")
    private Short runtimeMinutes = 0; // Cambiado a Short para coincidir con SMALLINT en PostgreSQL

    @Column(name = "genres", columnDefinition = "TEXT[]")
    private List<String> genres; // Cambiado a List<String> para reflejar el array en PostgreSQL

    @Column(name = "poster_url", length = 100)
    private String posterUrl;

    // Modificar la URL al obtener el valor
    public String getPosterUrl() {
        if (posterUrl != null && !posterUrl.isEmpty()) {
            return IMAGE_BASE_URL + posterUrl + IMAGE_SUFFIX;
        }
        return IMAGE_DEFAULT_URL;
    }
}
