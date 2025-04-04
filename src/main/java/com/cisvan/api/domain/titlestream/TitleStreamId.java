package com.cisvan.api.domain.titlestream;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TitleStreamId implements Serializable{

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "streaming_id", nullable = false)
    private Integer streamingId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleStreamId that = (TitleStreamId) o;
        return Objects.equals(tconst, that.tconst) &&
               Objects.equals(streamingId, that.streamingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tconst, streamingId);
    }
}
