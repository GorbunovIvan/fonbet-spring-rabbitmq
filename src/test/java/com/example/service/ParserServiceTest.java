package com.example.service;

import com.example.model.Game;
import com.example.model.League;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ParserServiceTest {

    private ParserService parserService;

    @Mock
    private AmqpTemplate mqTemplate;

    private String queue;
    private String URL;

    @BeforeEach
    void setUp() {

        queue = "testQueue";
        URL = "testURL";

        try (var ignored = MockitoAnnotations.openMocks(this)) {

            parserService = new ParserService(mqTemplate);

            var fieldQueue = parserService.getClass().getDeclaredField("queue");
            fieldQueue.setAccessible(true);
            fieldQueue.set(parserService, queue);

            var fieldURL = parserService.getClass().getDeclaredField("URL");
            fieldURL.setAccessible(true);
            fieldURL.set(parserService, URL);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSend() {

        var games = List.of(
                new Game(1L, new League("league 1"), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
                new Game(2L, new League("league 2"), new ArrayList<>(), LocalDateTime.now().plusHours(-2L), LocalDateTime.now().plusSeconds(1L))
        );

        parserService.send(games);

        for (var game : games) {
            verify(mqTemplate, times(1)).convertAndSend(queue, game);
        }
        verify(mqTemplate, times(games.size())).convertAndSend(anyString(), any(Game.class));
    }
}