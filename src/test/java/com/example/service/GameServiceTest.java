package com.example.service;

import com.example.config.RabbitMQConfig;
import com.example.model.Game;
import com.example.model.League;
import com.example.model.Team;
import com.example.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @SpyBean
    private GameRepository gameRepository;

    // Mocking, because I don't know of any other way to disable loading of these classes when the context is raised
    @MockBean
    private RabbitMQConfig rabbitMQConfig;
    @MockBean
    private RabbitMQListener rabbitMQListener;
    @MockBean
    private RabbitMessagingTemplate rabbitMessagingTemplate;
    @MockBean
    private RabbitListenerContainerFactory<?> rabbitListenerContainerFactory;
    @MockBean
    private ParserService parserService;

    private List<Game> gamesInDB;

    @BeforeEach
    void setUp() {
        gamesInDB = gameRepository.findAll();
        Mockito.reset(gameRepository);
    }

    @Test
    void testGetAll() {

        assertEquals(gamesInDB, gameService.getAll());
        assertEquals(1, gameService.getAll(1).size());
        assertEquals(0, gameService.getAll(0).size());

        verify(gameRepository, times(3)).findAll();
    }

    @Test
    void testGetById() {

        for (var game : gamesInDB) {
            assertEquals(game, gameService.getById(game.getId()));
            verify(gameRepository, times(1)).findById(game.getId());
        }

        assertNull(gameService.getById(-1L));
        verify(gameRepository, times(1)).findById(-1L);
    }

    @Test
    void testCreate() {

        // Trying to persist games that already exist (because they have no id, but by all other basic fields they have analogues in db)
        for (var gameInDB : gamesInDB) {

            var teams = gameInDB.getTeams().stream().map(t -> new Team(t.getName())).toList();

            var game = new Game();
            game.setLeague(new League(gameInDB.getLeagueName()));
            game.setTeams(teams);
            game.setDate(gameInDB.getDate());
            game.setParsedAt(gameInDB.getParsedAt());

            assertNull(gameService.create(game));
        }
        assertEquals(gamesInDB.size(), gameService.getAll().size());
        verify(gameRepository, times(gamesInDB.size())).merge(any(Game.class));
        Mockito.reset(gameRepository);

        // Trying to merge games that already exist (must work just like updating)
        for (var game : gamesInDB) {
            assertEquals(game, gameService.create(game));
        }
        assertEquals(gamesInDB.size(), gameService.getAll().size());
        verify(gameRepository, times(gamesInDB.size())).merge(any(Game.class));
        Mockito.reset(gameRepository);

        // Merging new game
        var game = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        game.setTeams(List.of(new Team("team 1"), new Team("new team")));

        var gameCreated = gameService.create(game);

        assertNotNull(gameCreated);
        assertNotNull(gameCreated.getId());
        assertEquals(game.getLeagueName(), gameCreated.getLeagueName());
        assertEquals(game.getDate(), gameCreated.getDate());
        assertEquals(game.getParsedAt(), gameCreated.getParsedAt());
        assertEquals(game.getTeamsString(), gameCreated.getTeamsString());

        assertEquals(gameService.getById(gameCreated.getId()), gameCreated);

        verify(gameRepository, times(1)).merge(any(Game.class));
    }

    @Test
    void testCreateAll() {

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

        assertTrue(gameService.createAll(gamesAsNew).isEmpty());
        assertEquals(gamesInDB.size(), gameService.getAll().size());
        verify(gameRepository, times(1)).mergeAll(anyList());
        Mockito.reset(gameRepository);

        // Trying to merge games that already exist (must work just like updating)
        assertFalse(gameService.createAll(gamesInDB).isEmpty());
        assertEquals(gamesInDB.size(), gameService.getAll().size());
        verify(gameRepository, times(1)).mergeAll(anyList());
        Mockito.reset(gameRepository);

        // Merging two new games
        List<Game> games = new ArrayList<>();

        var game1 = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        game1.setTeams(List.of(new Team("team 1"), new Team("new team")));
        games.add(game1);

        var game2 = new Game(null, new League("new league"), new ArrayList<>(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(5), LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS));
        game2.setTeams(List.of(new Team("new team"), new Team("team 2")));
        games.add(game2);

        List<Game> gameCreated = gameService.createAll(games);

        assertNotNull(gameCreated);
        assertEquals(games.size(), gameCreated.size());
        assertFalse(gameCreated.stream().anyMatch(gameMerged -> gameMerged.getId() == null));
        assertEquals(games.stream().map(Game::getLeagueName).toList(), gameCreated.stream().map(Game::getLeagueName).toList());
        assertEquals(games.stream().map(Game::getDate).toList(), gameCreated.stream().map(Game::getDate).toList());
        assertEquals(games.stream().map(Game::getParsedAt).toList(), gameCreated.stream().map(Game::getParsedAt).toList());
        assertEquals(games.stream().map(Game::getTeamsString).toList(), gameCreated.stream().map(Game::getTeamsString).toList());

        for (var gameMerged : gameCreated) {
            assertEquals(gameService.getById(gameMerged.getId()), gameMerged);
        }

        verify(gameRepository, times(1)).mergeAll(anyList());
    }

    @Test
    void testUpdate() {

        for (var game : gamesInDB) {
            game.setDate(game.getDate().plusHours(1).truncatedTo(ChronoUnit.SECONDS));
            var gameUpdatedInDB = gameService.update(game.getId(), game);
            assertEquals(game, gameUpdatedInDB);
            verify(gameRepository, times(1)).existsById(game.getId());
        }

        assertThrows(RuntimeException.class, () -> gameService.update(-1L, new Game()));
        verify(gameRepository, times(1)).existsById(-1L);
        verify(gameRepository, times(gamesInDB.size())).merge(any(Game.class));
    }

    @Test
    void testDelete() {

        gameService.delete(-1L);
        assertEquals(gamesInDB.size(), gameService.getAll().size());
        verify(gameRepository, times(1)).deleteById(-1L);

        for (var game : gamesInDB) {
            gameService.delete(game.getId());
            verify(gameRepository, times(1)).deleteById(game.getId());
        }

        assertTrue(gameService.getAll().isEmpty());
    }
}