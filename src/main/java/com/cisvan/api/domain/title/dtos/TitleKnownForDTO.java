package com.cisvan.api.domain.title.dtos;

import com.cisvan.api.domain.titlerating.TitleRating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleKnownForDTO {

    private static final String IMAGE_BASE_URL = "https://m.media-amazon.com/images/M/";
    private static final String IMAGE_SUFFIX = "._V1_SX300.jpg";
    private static final String IMAGE_DEFAULT_URL = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";
    
    private String tconst;
    private String titleType;
    private String primaryTitle;
    private Short startYear;
    private TitleRating titleRatings;
    private String posterUrl;

    // Modificar la URL al obtener el valor
    public String getPosterUrl() {
        if (posterUrl == null || posterUrl.isEmpty()) {
            return IMAGE_DEFAULT_URL;
        }
    
        // Si ya contiene una URL completa, la retorna tal cual
        if (posterUrl.startsWith("http://") || posterUrl.startsWith("https://")) {
            return posterUrl;
        }
    
        // Si no, construye la URL basada en el patrón por defecto
        return IMAGE_BASE_URL + posterUrl + IMAGE_SUFFIX;
    }    
}
