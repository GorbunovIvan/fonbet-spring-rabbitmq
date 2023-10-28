package com.example.repository;

import com.example.model.Game;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long>, GameRepositoryCustom {

    @Query("FROM Game games " +
            "LEFT JOIN FETCH games.gamesTeams gamesTeams1 " +
            "LEFT JOIN FETCH games.league leagues " +
            "LEFT JOIN FETCH gamesTeams1.team " +
            "ORDER BY leagues.name, games.date DESC")
    @Nonnull
    List<Game> findAll();

    @Query("FROM Game games " +
            "LEFT JOIN FETCH games.gamesTeams gamesTeams1 " +
            "LEFT JOIN FETCH games.league leagues " +
            "LEFT JOIN FETCH gamesTeams1.team " +
            "WHERE games.id = :id")
    @Nonnull
    Optional<Game> findById(@Nullable @Param("id") Long id);
}
