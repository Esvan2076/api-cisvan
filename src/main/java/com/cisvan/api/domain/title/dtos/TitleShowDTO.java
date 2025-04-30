package com.cisvan.api.domain.title.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleShowDTO {

    private static final String IMAGE_BASE_URL = "https://m.media-amazon.com/images/M/";
    private static final String IMAGE_SUFFIX = "._V1_SX300.jpg";
    private static final String IMAGE_DEFAULT_URL = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";

    private String tconst;
    private String primaryTitle;
    private String posterUrl;
    private BigDecimal averageRating;

    @Builder.Default
    private boolean inUserList = false;

    // Modificar la URL al obtener el valor
    public String getPosterUrl() {
        if (posterUrl == null || posterUrl.isEmpty()) {
            return IMAGE_DEFAULT_URL;
        }
        if (posterUrl.startsWith("http://") || posterUrl.startsWith("https://")) {
            return posterUrl;
        }
        return IMAGE_BASE_URL + posterUrl + IMAGE_SUFFIX;
    }

    // Constructor extra que Hibernate necesita
    public TitleShowDTO(String tconst, String primaryTitle, String posterUrl, BigDecimal averageRating) {
        this.tconst = tconst;
        this.primaryTitle = primaryTitle;
        this.posterUrl = posterUrl;
        this.averageRating = averageRating;
        this.inUserList = false; // Siempre default
    }
}