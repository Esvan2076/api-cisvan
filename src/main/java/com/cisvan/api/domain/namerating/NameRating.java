package com.cisvan.api.domain.namerating;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "name_ratings")
public class NameRating {

    @Id
    @Column(name = "nconst", nullable = false, length = 15)
    private String nconst;

    @Column(name = "average_rating", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2) DEFAULT 0.00")
    private BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Column(name = "num_votes", columnDefinition = "INT DEFAULT 0")
    private Integer numVotes = 0;
}
