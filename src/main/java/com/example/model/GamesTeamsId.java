package com.example.model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class GamesTeamsId implements Serializable {
    private Long game;
    private Integer team;
}
