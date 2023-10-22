package com.example.repository;

import com.example.model.Game;

import java.util.Collection;
import java.util.List;

public interface GameRepositoryCustom {
    Game merge(Game game);
    List<Game> mergeAll(Collection<Game> games);
}
