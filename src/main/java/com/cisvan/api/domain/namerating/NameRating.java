package com.cisvan.api.domain.namerating;

import java.math.BigDecimal;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "name_ratings")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NameRating {

    @Id
    @Column(name = "nconst", nullable = false, length = 15)
    private String nconst;

    @Builder.Default
    @Column(name = "average_rating", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2) DEFAULT 0.00")
    private BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Builder.Default
    @Column(name = "num_votes", columnDefinition = "INT DEFAULT 0")
    private Integer numVotes = 0;
}