package com.cisvan.api.domain.reviews.userTitleRating;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTitleRatingId implements Serializable {
    
    private Long reviewId;
    private String tconst;
}