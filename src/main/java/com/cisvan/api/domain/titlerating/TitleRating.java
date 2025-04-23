package com.cisvan.api.domain.titlerating;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "title_ratings")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleRating {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Builder.Default
    @Column(name = "average_rating", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2) DEFAULT 0.00")
    private BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Builder.Default
    @Column(name = "num_votes", columnDefinition = "INT DEFAULT 0")
    private Integer numVotes = 0;
}