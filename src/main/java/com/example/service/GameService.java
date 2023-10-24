package com.example.service;

import com.example.model.Game;
import com.example.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public List<Game> getAll() {
        return gameRepository.findAll();
    }

    public List<Game> getAll(int limit) {
        return getAll()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Game getById(Long id) {
        return gameRepository.findById(id)
                .orElse(null);
    }

    public Game create(Game game) {
        return gameRepository.merge(game);
    }

    public List<Game> createAll(Collection<Game> games) {
        return gameRepository.mergeAll(games);
    }

    @Transactional
    public Game update(Long id, Game game) {
        if (!gameRepository.existsById(id)) {
            throw new RuntimeException("Game with id '" + id + "' not found");
        }
        game.setId(id);
        return gameRepository.merge(game);
    }

    public void delete(Long id) {
        gameRepository.deleteById(id);
    }
}
