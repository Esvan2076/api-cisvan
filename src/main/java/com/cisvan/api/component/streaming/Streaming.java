package com.cisvan.api.component.streaming;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "streaming_services")
public class Streaming {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false, length = 7)
    private String color = "#000000";

    @Column(name = "url", length = 255)
    private String url;
}