package com.cisvan.api.domain.titlestream;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TitleStreamId implements Serializable {

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "streaming_id", nullable = false)
    private Integer streamingId;
}
