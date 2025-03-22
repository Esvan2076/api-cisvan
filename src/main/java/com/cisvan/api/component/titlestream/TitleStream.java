package com.cisvan.api.component.titlestream;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
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
