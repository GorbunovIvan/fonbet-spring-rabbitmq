package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = "name")
@ToString
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true)
    private String name;

    public Team(String name) {
        this.name = name;
    }
}
