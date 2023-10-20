package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "games_teams")
@IdClass(GamesTeamsId.class)
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class GamesTeams {

    @Id
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "game_id")
    private Game game;

    @Id
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "team_id")
    private Team team;
}
