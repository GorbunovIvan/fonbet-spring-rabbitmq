package com.example.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "games_teams")
@IdClass(GamesTeamsId.class)
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class GamesTeams implements Serializable {

    @Id
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "game_id")
    @JsonBackReference
    private Game game;

    @Id
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "team_id")
    private Team team;
}
