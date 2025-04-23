package com.cisvan.api.domain.name;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "name_basics")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Name {

    private static final String IMAGE_BASE_URL = "https://media.themoviedb.org/t/p/w300_and_h450_bestv2";
    private static final String IMAGE_DEFAULT_URL = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";

    @Id
    @Column(name = "nconst", length = 15, nullable = false)
    private String nconst;

    @Column(name = "primary_name", nullable = false)
    private String primaryName;

    @Column(name = "birth_year")
    private Short birthYear;

    @Column(name = "death_year")
    private Short deathYear;

    @Column(name = "primary_profession", columnDefinition = "TEXT[]")
    private List<String> primaryProfession;

    @Builder.Default
    @Column(name = "known_for_titles", columnDefinition = "TEXT[]")
    private List<String> knownForTitles = new ArrayList<>();

    @Column(name = "image_url", length = 100)
    private String imageUrl;

    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return IMAGE_BASE_URL + imageUrl;
        }
        return IMAGE_DEFAULT_URL;
    }
}