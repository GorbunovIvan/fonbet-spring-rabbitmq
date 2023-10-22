package com.example.repository;

import com.example.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long>, GameRepositoryCustom {
}
