package com.cisvan.api.domain.reviews.userNameRating;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNameRatingId implements Serializable {
    
    private Long reviewId;
    private String nconst;
}
