package com.cisvan.api.component.title;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_basics")
public class TitleBasics {

    private static final String IMAGE_BASE_URL = "https://m.media-amazon.com/images/M/";

    @Id
    @Column(name = "tconst", nullable = false)
    private String tconst;

    @Column(name = "titletype", nullable = false)
    private String titleType;

    @Column(name = "primarytitle", nullable = false)
    private String primaryTitle;

    @Column(name = "originaltitle", nullable = false)
    private String originalTitle;

    @Column(name = "isadult", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdult = false;

    @Column(name = "startyear")
    private Integer startYear;

    @Column(name = "endyear")
    private Integer endYear;

    @Column(name = "runtimeminutes", columnDefinition = "INT DEFAULT 0")
    private Integer runtimeMinutes = 0;

    @Column(name = "genres")
    private String genres;

    @Column(name = "poster_url")
    private String posterUrl;

    // Modificar la URL al obtener el valor
    public String getPosterUrl() {
        if (posterUrl != null && !posterUrl.isEmpty()) {
            return IMAGE_BASE_URL + posterUrl + "@._V1_SX500.jpg";
        }
        return null; // O podrías devolver una imagen por defecto si es necesario
    }
}
