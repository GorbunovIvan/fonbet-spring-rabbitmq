package com.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @OneToMany(mappedBy = "game", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<GamesTeams> gamesTeams = new ArrayList<>();

    public List<Team> getTeams() {
        return gamesTeams.stream()
                .map(GamesTeams::getTeam)
                .toList();
    }

    public void setTeams(List<Team> teams) {
        gamesTeams.clear();
        for (var team : teams) {
            var newGamesTeams = new GamesTeams(this, team);
            gamesTeams.add(newGamesTeams);
        }
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", league=" + league +
                ", parsed-at=" + parsedAt +
                ", teams=" + getTeams() +
                '}';
    }
}
