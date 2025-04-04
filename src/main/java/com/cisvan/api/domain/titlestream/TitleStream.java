package com.cisvan.api.domain.titlestream;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Title_Streaming_Relation")
public class TitleStream {

    @EmbeddedId
    private TitleStreamId titleStreamId;
}
