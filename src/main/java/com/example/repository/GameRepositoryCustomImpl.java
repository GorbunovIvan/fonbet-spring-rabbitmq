package com.example.repository;

import com.example.model.Game;
import com.example.model.League;
import com.example.model.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameRepositoryCustomImpl implements GameRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public Game merge(Game game) {

        var leaguesNames = List.of(game.getLeague().getName());
        var teamsNames = game.getTeams().stream().map(Team::getName).toList();

        Map<String, League> leaguesByNames = findAllLeaguesByNames(leaguesNames);
        Map<String, Team> teamsByNames = findAllTeamsByNames(teamsNames);
        
        // Checking if this game is already in DB
        if (game.getId() == null) {
            var gamesExisting = allGamesOfLeaguesForDays(new ArrayList<>(leaguesByNames.values()), Set.of(game.getDate().toLocalDate()));
            Map<Game, Game> gamesPersistingToGamesExisting = mapExistingGamesToPersistingGames(List.of(game), gamesExisting);
            if (!gamesPersistingToGamesExisting.isEmpty()) {
                log.warn("Game is already in DB with id '" + gamesPersistingToGamesExisting.get(game).getId() + "': '" + game + "'");
                return null;
            }
        }

        return mergeEntity(game, leaguesByNames, teamsByNames);
    }

    @Transactional
    @Override
    public List<Game> mergeAll(Collection<Game> gamesInit) {

        var games = new ArrayList<>(gamesInit);

        var leaguesNames = games.stream().map(Game::getLeagueName).distinct().toList();
        var teamsNames = games.stream().map(Game::getTeams).flatMap(List::stream).map(Team::getName).distinct().toList();

        Map<String, League> leaguesByNames = findAllLeaguesByNames(leaguesNames);
        Map<String, Team> teamsByNames = findAllTeamsByNames(teamsNames);
        Set<LocalDate> days = games.stream().map(game -> game.getDate().toLocalDate()).collect(Collectors.toSet());

        // Checking if this game is already in DB
        var gamesExisting = allGamesOfLeaguesForDays(new ArrayList<>(leaguesByNames.values()), days);
        Map<Game, Game> gamesPersistingToGamesExisting = mapExistingGamesToPersistingGames(games, gamesExisting);
        gamesPersistingToGamesExisting.keySet().removeIf(gamePersisting -> gamePersisting.getId() != null);
        if (!gamesPersistingToGamesExisting.isEmpty()) {
            var gamesToRemove = gamesPersistingToGamesExisting.keySet();
            games.removeAll(gamesToRemove);
            for (var game : gamesToRemove) {
                log.warn("Game is already in DB with id '" + gamesPersistingToGamesExisting.get(game).getId() + "': '" + game + "'");
            }
        }

        if (games.isEmpty()) {
            return Collections.emptyList();
        }

        List<Game> gamesMerged = new ArrayList<>();

        for (var game : games) {
            var gameMerged = mergeEntity(game, leaguesByNames, teamsByNames);
            gamesMerged.add(gameMerged);
        }

        return gamesMerged;
    }

    private Game mergeEntity(Game game,
                             Map<String, League> leaguesByNames,
                             Map<String, Team> teamsByNames) {

        // League
        Objects.requireNonNull(game.getLeague());
        League league = game.getLeague();
        String leagueName = league.getName();
        if (leaguesByNames.containsKey(leagueName)) {
            league = leaguesByNames.get(leagueName);
        }
        league = entityManager.merge(league);
        game.setLeague(league);
        leaguesByNames.put(leagueName, league);

        // Teams
        var teams = new ArrayList<>(game.getTeams());
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            String teamName = team.getName();
            if (teamsByNames.containsKey(teamName)) {
                team = teamsByNames.get(teamName);
            }
            team = entityManager.merge(team);
            teams.set(i, team);
            teamsByNames.put(teamName, team);
        }
        game.setTeams(teams);

        return entityManager.merge(game);
    }

    private Map<String, League> findAllLeaguesByNames(List<String> names) {
        return entityManager.createQuery("FROM League WHERE name IN (:names)", League.class)
                .setParameter("names", names)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(League::getName, Function.identity()));
    }

    private Map<String, Team> findAllTeamsByNames(List<String> names) {
        return entityManager.createQuery("FROM Team WHERE name IN (:names)", Team.class)
                .setParameter("names", names)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(Team::getName, Function.identity()));
    }

    private List<Game> allGamesOfLeaguesForDays(List<League> leagues, Set<LocalDate> localDates) {

        Set<Date> days = localDates.stream().map(Date::valueOf).collect(Collectors.toSet());

        return entityManager.createQuery("FROM Game game " +
                        "WHERE FUNCTION('DATE_TRUNC', 'DAY', game.date) IN (:days) " +
                        "AND game.league IN (:leagues)", Game.class)
                .setParameter("days", days)
                .setParameter("leagues", leagues)
                .getResultList();
    }

    /**
     * @return the map, where key is persisting game and value is existing game
     */
    private Map<Game, Game> mapExistingGamesToPersistingGames(List<Game> gamesPersisting, List<Game> gamesExisting) {

        var map = new HashMap<Game, Game>();

        for (var game : gamesPersisting) {
            for (var gameExisting : gamesExisting) {

                // Comparing leagues
                if (game.getLeague().getName().equals(gameExisting.getLeague().getName())) {

                    // Comparing dates
                    Duration duration = Duration.between(game.getDate(), gameExisting.getDate());

                    if (Math.abs(duration.toSeconds()) < 60 * 60 * 6) {

                        // Comparing teams
                        var teamsOfGame = game.getTeams().stream().map(Team::getName).sorted().toList();
                        var teamsOfGameExisting = gameExisting.getTeams().stream().map(Team::getName).sorted().toList();

                        if (teamsOfGame.equals(teamsOfGameExisting)) {
                            map.put(game, gameExisting);
                            break;
                        }
                    }
                }
            }
        }

        return map;
    }
}
