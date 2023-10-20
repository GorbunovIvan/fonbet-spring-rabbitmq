package com.example.service;

import com.example.model.Game;
import com.example.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public List<Game> getAll() {
        return gameRepository.findAll();
    }

    public Game getById(Long id) {
        return gameRepository.findById(id)
                .orElse(null);
    }

    public Game create(Game game) {
        return gameRepository.save(game);
    }

    @Transactional
    public Game update(Long id, Game game) {
        if (!gameRepository.existsById(id)) {
            throw new RuntimeException("Game with id '" + id + "' not found");
        }
        game.setId(id);
        return gameRepository.save(game);
    }

    public void delete(Long id) {
        gameRepository.deleteById(id);
    }
}
