package com.cisvan.api.component.ratings;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "title_ratings")
public class TitleRatings {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "average_rating", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2) DEFAULT 0.00")
    private BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Column(name = "num_votes", columnDefinition = "INT DEFAULT 0")
    private Integer numVotes = 0;
}
