package com.example.service;

import com.example.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RabbitMQListenerTest {

    private RabbitMQListener rabbitMQListener;

    @Mock
    private GameService gameService;
    @Mock
    private Message<Game> message;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            rabbitMQListener = new RabbitMQListener(gameService);
            when(message.getPayload()).thenReturn(new Game());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProcessMyQueue() {

        rabbitMQListener.processMyQueue(message);

        verify(message, times(1)).getPayload();
        verify(gameService, times(1)).create(any(Game.class));
    }
}