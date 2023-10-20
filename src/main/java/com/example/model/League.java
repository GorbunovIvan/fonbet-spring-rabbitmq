package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leagues")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "name")
@ToString
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true)
    private String name;

    public League(String name) {
        this.name = name;
    }
}
