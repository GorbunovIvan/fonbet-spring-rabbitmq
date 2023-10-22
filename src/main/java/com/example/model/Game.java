package com.example.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "games")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Game implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "league_id")
    private League league;

    @OneToMany(mappedBy = "game", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<GamesTeams> gamesTeams = new ArrayList<>();

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @EqualsAndHashCode.Include
    @ToString.Include
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

    public String getLeagueName() {
        var league = getLeague();
        if (league == null) {
            return "";
        }
        return league.getName();
    }

    public String getTeamsString() {
        return getTeams().stream()
                .map(Team::getName)
                .collect(Collectors.joining(" - "));
    }
}
