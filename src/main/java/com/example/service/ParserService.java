package com.example.service;

import com.example.model.Game;
import com.example.model.League;
import com.example.model.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParserService {

    private final AmqpTemplate mqTemplate;

    @Value("${spring.rabbitmq.queue-parser}")
    private String queue;

    @Value("${parser.URL}")
    private String URL;

    @Scheduled(fixedRateString = "${parser.scheduled.fixedRate.milliseconds}")
    public void parseAndSend() {
        var games = parse();
        send(games);
    }

    protected List<Game> parse() {

        List<Game> games = new ArrayList<>();

        String html = getHtmlContent(URL);

        Document doc = Jsoup.parse(html);
        var leaguesBlocks = doc.select("div.results-league");

        for (var leagueBlock : leaguesBlocks) {

            var leagueTitleBlock = leagueBlock.selectFirst("div.results-league-title p");
            if (leagueTitleBlock == null) {
                log.error("League title was not found");
                continue;
            }

            League league = new League(leagueTitleBlock.text());

            var linksToMatches = leagueBlock.select("a.results-match-link");

            for (var linkToMatch : linksToMatches) {

                // Date
                var dateBlock = leagueBlock.selectFirst("span.results-time");
                if (dateBlock == null) {
                    log.error("Date was not found for league '" + league.getName() + "'");
                    continue;
                }

                LocalDateTime date;
                String dateText = dateBlock.text();
                if (dateText.contains(":")) {
                    var time = LocalTime.parse(dateBlock.text());
                    date = LocalDateTime.of(LocalDate.now(), time);
                } else {
                    String numbersOnly = dateText.replaceAll("[^0-9.]", "");
                    if (numbersOnly.isEmpty()) {
                        numbersOnly = "90";
                    }
                    int minutesAgo = Integer.parseInt(numbersOnly);
                    date = LocalDateTime.now().minusMinutes(minutesAgo).truncatedTo(ChronoUnit.MINUTES);
                }

                // Extracting two teams
                var team1Element = linkToMatch.selectFirst("span.team.team_1 ");
                var team2Element = linkToMatch.selectFirst("span.team.team_2 ");

                if (team1Element == null || team2Element == null) {
                    log.error("Teams were not found for league '" + league.getName() + "' and time '" + date + "'");
                    continue;
                }

                List<Team> teams = new ArrayList<>();
                teams.add(new Team(team1Element.text()));
                teams.add(new Team(team2Element.text()));

                // Putting it all together
                var game = new Game();
                game.setLeague(league);
                game.setTeams(teams);
                game.setDate(date);
                game.setParsedAt(LocalDateTime.now());

                games.add(game);
            }
        }

        return games;
    }

    private String getHtmlContent(String URL) {

        var httpGet = new HttpGet(URL);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
            var httpEntity = response.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void send(List<Game> games) {
        for (var game : games) {
            mqTemplate.convertAndSend(queue, game);
            log.info("Game is added to queue: '" + game + "'");
        }
    }
}
