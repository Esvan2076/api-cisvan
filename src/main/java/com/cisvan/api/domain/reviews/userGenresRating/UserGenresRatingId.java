package com.cisvan.api.domain.reviews.userGenresRating;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGenresRatingId implements Serializable {
    
    private Long reviewId;
    private String tconst;
    private String genre;
}
