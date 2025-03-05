package com.cisvan.api.component.ratings;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "title_ratings")
public class TitleRatings {

    @Id
    @Column(name = "tconst", nullable = false)
    private String tconst;

    @Column(name = "averagerating", precision = 3, scale = 1, columnDefinition = "DECIMAL(3,1) DEFAULT 0.0")
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Column(name = "numvotes", columnDefinition = "INT DEFAULT 0")
    private Integer numVotes = 0;
}
