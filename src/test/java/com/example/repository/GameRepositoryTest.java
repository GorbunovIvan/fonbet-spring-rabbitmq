package com.example.repository;

import com.example.model.Game;
import com.example.model.League;
import com.example.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    private List<Game> gamesInDB;

    @BeforeEach
    void setUp() {
        gamesInDB = gameRepository.findAll();
    }

    @Test
    void testMerge() {

        // Trying to persist games that already exist (because they have no id, but by all other basic fields they have analogues in db)
        for (var gameInDB : gamesInDB) {

            var teams = gameInDB.getTeams().stream().map(t -> new Team(t.getName())).toList();

            var game = new Game();
            game.setLeague(new League(gameInDB.getLeagueName()));
            game.setTeams(teams);
            game.setDate(gameInDB.getDate());
            game.setParsedAt(gameInDB.getParsedAt());

            assertNull(gameRepository.merge(game));
        }
        assertEquals(gamesInDB.size(), gameRepository.findAll().size());

        // Trying to merge games that already exist (must work just like updating)
        for (var game : gamesInDB) {
            assertEquals(game, gameRepository.merge(game));
        }
        assertEquals(gamesInDB.size(), gameRepository.findAll().size());

        // Merging new game
        var game = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        game.setTeams(List.of(new Team("team 1"), new Team("new team")));

        var gameMerged = gameRepository.merge(game);

        assertNotNull(gameMerged);
        assertNotNull(gameMerged.getId());
        assertEquals(game.getLeagueName(), gameMerged.getLeagueName());
        assertEquals(game.getDate(), gameMerged.getDate());
        assertEquals(game.getParsedAt(), gameMerged.getParsedAt());
        assertEquals(game.getTeamsString(), gameMerged.getTeamsString());

        assertEquals(gameRepository.findById(gameMerged.getId()).orElse(null), gameMerged);
    }

    @Test
    void testMergeAll() {

        // Trying to persist games that already exist (because they have no id, but by all other basic fields they have analogues in db)
        var gamesAsNew = new ArrayList<Game>();
        for (var gameInDB : gamesInDB) {

            var teams = gameInDB.getTeams().stream().map(t -> new Team(t.getName())).toList();

            var game = new Game();
            game.setLeague(new League(gameInDB.getLeagueName()));
            game.setTeams(teams);
            game.setDate(gameInDB.getDate());
            game.setParsedAt(gameInDB.getParsedAt());

            gamesAsNew.add(game);
        }

        assertTrue(gameRepository.mergeAll(gamesAsNew).isEmpty());
        assertEquals(gamesInDB.size(), gameRepository.findAll().size());

        // Trying to merge games that already exist (must work just like updating)
        assertFalse(gameRepository.mergeAll(gamesInDB).isEmpty());
        assertEquals(gamesInDB.size(), gameRepository.findAll().size());

        // Merging two new games
        List<Game> games = new ArrayList<>();

        var game1 = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        game1.setTeams(List.of(new Team("team 1"), new Team("new team")));
        games.add(game1);

        var game2 = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(5), LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS));
        game2.setTeams(List.of(new Team("new team"), new Team("team 2")));
        games.add(game2);

        List<Game> gamesMerged = gameRepository.mergeAll(games);

        assertNotNull(gamesMerged);
        assertEquals(games.size(), gamesMerged.size());
        assertFalse(gamesMerged.stream().anyMatch(gameMerged -> gameMerged.getId() == null));
        assertEquals(games.stream().map(Game::getLeagueName).toList(), gamesMerged.stream().map(Game::getLeagueName).toList());
        assertEquals(games.stream().map(Game::getDate).toList(), gamesMerged.stream().map(Game::getDate).toList());
        assertEquals(games.stream().map(Game::getParsedAt).toList(), gamesMerged.stream().map(Game::getParsedAt).toList());
        assertEquals(games.stream().map(Game::getTeamsString).toList(), gamesMerged.stream().map(Game::getTeamsString).toList());

        for (var gameMerged : gamesMerged) {
            assertEquals(gameRepository.findById(gameMerged.getId()).orElse(null), gameMerged);
        }
    }
}